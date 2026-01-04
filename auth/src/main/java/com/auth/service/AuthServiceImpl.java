package com.auth.service;

import com.auth.dtos.*;
import com.auth.entities.Admin;
import com.auth.entities.Seller;
import com.auth.entities.User;
import com.auth.exception.UserAlreadyRegisteredException;
import com.auth.repository.AdminRepository;
import com.auth.repository.SellerRepository;
import com.auth.repository.UserRepository;
import com.auth.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {

    private UserRepository userRepository;
    private SellerRepository sellerRepository;
    private AdminRepository adminRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager userAuthenticationManager;
    private AuthenticationManager sellerAuthenticationManager;
    private AuthenticationManager adminAuthenticationManager;
    private JwtUtil jwtUtil;

    public AuthServiceImpl(@Qualifier("userAuthenticationManager") AuthenticationManager userAuthenticationManager,@Qualifier("sellerAuthenticationManager") AuthenticationManager sellerAuthenticationManager,@Qualifier("adminAuthenticationManager") AuthenticationManager adminAuthenticationManager,UserRepository userRepository,SellerRepository sellerRepository,AdminRepository adminRepository,PasswordEncoder passwordEncoder,JwtUtil jwtUtil){
        this.sellerAuthenticationManager=sellerAuthenticationManager;
        this.userAuthenticationManager=userAuthenticationManager;
        this.adminAuthenticationManager=adminAuthenticationManager;
        this.sellerRepository=sellerRepository;
        this.userRepository=userRepository;
        this.adminRepository=adminRepository;
        this.jwtUtil=jwtUtil;
        this.passwordEncoder=passwordEncoder;
    }

    @Override
    public RegisterOutput register(UserInputDTO dto) {
        if(userRepository.findByUsername(dto.getUsername()).isPresent()){
            throw new UserAlreadyRegisteredException("UserAlready registered");
        }
        User user=new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return new RegisterOutput("User registed successfully");
    }

    @Override
    public UserOutputDTO login(UserInputDTO dto) {

        UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(dto.getUsername(),dto.getPassword());
        Authentication authentication=userAuthenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Long Userid=userRepository.findByUsername(dto.getUsername()).get().getId();
        String token=jwtUtil.generateToken(authentication,Userid);
        return new UserOutputDTO("Login Successfully",token);
    }



    @Override
    public RegisterOutput sellerRegister(SellerRegister dto) {
        if(sellerRepository.findByUsername(dto.getUsername()).isPresent()){
            throw new UserAlreadyRegisteredException("UserAlready registered");
        }
        Seller seller=new Seller();
        seller.setUsername(dto.getUsername());
        seller.setPassword(passwordEncoder.encode(dto.getPassword()));
        seller.setRole("ROLE_SELLER");
        sellerRepository.save(seller);
        return new RegisterOutput("Seller registed successfully");

    }

    @Override
    public SellerOutputDTO sellerLogin(SellerLoginDTO dto) {
        UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(dto.getUsername(),dto.getPassword());
        Authentication authentication=sellerAuthenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Long Userid=sellerRepository.findByUsername(dto.getUsername()).get().getId();
        String token=jwtUtil.generateToken(authentication,Userid);
        return new SellerOutputDTO("Login Successfully",token);
    }

    @Override
    public AdminLoginResponse adminLogin(AdminLoginDTO dto){
        UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(dto.getUsername(),dto.getPassword());
        Authentication authentication=adminAuthenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Long Userid=adminRepository.findByUsername(dto.getUsername()).orElseThrow(()->new UsernameNotFoundException("Admin not found in DB")).getId();
        String token= jwtUtil.generateToken(authentication,Userid);
        return new AdminLoginResponse(token,"Login Successfully");
    }

    @Override
    public RegisterOutput adminRegister(AdminLoginDTO adminLoginDTO) {
        // 1. Check if the admin already exists
        if (adminRepository.findByUsername(adminLoginDTO.getUsername()).isPresent()) {
            throw new UserAlreadyRegisteredException("Admin username already exists!");
        }

        // 2. Create and Save Admin
        Admin admin = new Admin();
        admin.setUsername(adminLoginDTO.getUsername());
        admin.setPassword(passwordEncoder.encode(adminLoginDTO.getPassword()));
        admin.setRole("ROLE_ADMIN"); // Ensure the ROLE_ prefix is here

        adminRepository.save(admin);

        // 3. Return the specific message structure you requested
        return new RegisterOutput("Admin registered succesfully");
    }
}
