package com.UPOX.upox_back_end.service;

import com.UPOX.upox_back_end.dto.request.ActivateRequest;
import com.UPOX.upox_back_end.dto.request.AuthenticateRequest;
import com.UPOX.upox_back_end.dto.request.IntrospectRequest;
import com.UPOX.upox_back_end.dto.response.AuthenticateResponse;
import com.UPOX.upox_back_end.dto.response.IntrospectResponse;
import com.UPOX.upox_back_end.entity.User;
import com.UPOX.upox_back_end.exception.ErrorCode;
import com.UPOX.upox_back_end.model.EmailForm;
import com.UPOX.upox_back_end.model.Otp;
import com.UPOX.upox_back_end.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.time.temporal.*;

@Service
public class AuthenticateService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private TemplateEngine templateEngine;


    @NonFinal //Secret key để generate signature
    @Value("${jwt.secret.key}")
    protected String SECRET;


    @NonFinal
    @Value("${spring.mail.username}")
    String sender;


    public void sendMail(String recipient, String otpCode){
        try{
            String otp = otpCode.replace(""," ").trim();
            System.out.println(otp);

            EmailForm emailForm = new EmailForm(recipient,otp,"Cho chúng tôi biết đây là bạn.");
            Context context = new Context();
            context.setVariable("otpCode",otp);


            String htmlForm = templateEngine.process("htmlForm",context); //Lâu ở render mail


            MimeMessage mailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage,true);
            helper.setFrom(sender);
            helper.setTo(emailForm.getRecipient());
            helper.setSubject(emailForm.getSubject());
            helper.setText(htmlForm,true);

            javaMailSender.send(mailMessage);  //Lâu ở gửi mail

        }catch (Exception ex){
            System.out.println("Gửi không thành công!");
        }
    }

    public Otp getOtpResponse(){
        String otp = generateOtp();
        String expiredOtp = generateExpiredOtp();
        return new Otp(otp,expiredOtp);
    }

    private String getRandomString(String keyEnv){
        String SALTCHARS = keyEnv;
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 4) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String randStr = salt.toString();
        return randStr;
    }

    private String generateOtp(){
        String otp = getRandomString("1234567890");
        return otp;
    }
    private String generateExpiredOtp(){
        String expiredOtp = getRandomString("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
        return expiredOtp;
    }

    public AuthenticateResponse activate(ActivateRequest request){
        var user =  userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_EXISTED.getMessage()));
        if(!request.isOtpVerified() || user.getEmail().isEmpty()){ //Có nhập mail và xác thực OTP thành công
            throw new RuntimeException(ErrorCode.USER_NOT_AUTHENTICATED.getMessage());
        }
        var token = generateToken(user.getUsername(),user.getId());
        return AuthenticateResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request){
        var user =  userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_EXISTED.getMessage()));


        //Xét password có trùng nhau không
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        //Đăng nhập sai
        if(!isAuthenticated){
            throw new RuntimeException(ErrorCode.USER_NOT_AUTHENTICATED.getMessage());
        }

        //Đăng nhập đúng --> cấp token
        var token = generateToken(user.getUsername(),user.getId());
        return AuthenticateResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }

    private String generateToken(String userName, String userId){
        //Header
        JWSHeader jwtHeader = new JWSHeader(JWSAlgorithm.HS512);


        //Payload
        //Data trong body gọi là claim
        LocalDateTime expiration = LocalDateTime.now(); //Set expiry date
        Date convertedExpirydate = Date.from(expiration.plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userName)
                .issuer("UPOX.com")   //Được gửi/issue từ ai (thường là domain của business)
                .issueTime(new Date())
                .expirationTime(convertedExpirydate)
                .claim("userId", userId)
                .build();

        Payload jwtPayLoad = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwtHeader,jwtPayLoad);


        //Signature
        //Khoá để ký - Khoá giải mã trùng nhau: Symmetric Encryption
        //Khoá để ký - Khoá public để verify: Asymmetric Encryption

        try {
            jwsObject.sign(new MACSigner(SECRET.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            System.out.println("Cannot create token");
            throw new RuntimeException(e);
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        //verify
        JWSVerifier verifier = new MACVerifier(SECRET.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean isTokenVerified = signedJWT.verify(verifier);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean isTokenStillValid = expiryTime.after(new Date());

        return IntrospectResponse.builder()
                .valid(isTokenVerified && isTokenStillValid)
                .build();
    }


}
