package com.auth.service;

import com.auth.entities.Admin;
import com.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomAdminDetailService implements UserDetailsService {

    private final AdminRepository adminRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin=adminRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("Sorry Username is not found"));
        return User.builder()
                .username(admin.getUsername())
                .authorities(admin.getRole())
                .password(admin.getPassword())
                .build();
    }
//@Override
//public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//    System.out.println("Login attempt for admin username: "+username);
//    Admin admin=adminRepository.findByUsername(username).orElseThrow(()->{
//
//        System.out.println("Database search failed for: "+username);
//        return new UsernameNotFoundException("Sorry Username is not found");
//    });
//    System.out.println("User found! Role is: "+admin.getRole());
//
//    return User.builder()
//            .username(admin.getUsername())
//            .authorities(admin.getRole())
//            .password(admin.getPassword())
//            .build();
//}
}
