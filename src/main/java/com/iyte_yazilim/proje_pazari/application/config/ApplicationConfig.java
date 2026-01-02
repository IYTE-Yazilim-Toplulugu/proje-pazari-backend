package com.iyte_yazilim.proje_pazari.application.config;

import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectHandler;
import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectValidator;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserHandler;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserValidator;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserHandler;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserValidator;
import com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail.ResendVerificationEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail.ResendVerificationEmailHandler;
import com.iyte_yazilim.proje_pazari.application.commands.verifyEmail.VerifyEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.verifyEmail.VerifyEmailHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.CreateProjectMapper;
import com.iyte_yazilim.proje_pazari.application.mappers.RegisterUserMapper;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.application.queries.getAllUsers.GetAllUsersHandler;
import com.iyte_yazilim.proje_pazari.application.queries.getAllUsers.GetAllUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUser.GetUserHandler;
import com.iyte_yazilim.proje_pazari.application.queries.getUser.GetUserQuery;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.VerifyEmailResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    // ========== Domain Services ==========
    @Bean
    public com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService
            verificationTokenService() {
        return new com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService();
    }

    // ========== Project Beans ==========
    @Bean
    public IValidator<CreateProjectCommand> createProjectValidator() {
        return new CreateProjectValidator();
    }

    @Bean
    public IRequestHandler<CreateProjectCommand, ApiResponse<CreateProjectCommandResult>>
            createProjectHandler(
                    ProjectRepository projectRepository,
                    UserRepository userRepository,
                    IValidator<CreateProjectCommand> validator,
                    CreateProjectMapper createProjectMapper,
                    ProjectMapper projectMapper,
                    com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper
                            userMapper) {
        return new CreateProjectHandler(
                projectRepository,
                userRepository,
                validator,
                createProjectMapper,
                projectMapper,
                userMapper);
    }

    // ========== User Auth Beans ==========
    @Bean
    public IValidator<RegisterUserCommand> registerUserValidator() {
        return new RegisterUserValidator();
    }

    @Bean
    public IValidator<LoginUserCommand> loginUserValidator() {
        return new LoginUserValidator();
    }

    @Bean
    public IRequestHandler<RegisterUserCommand, ApiResponse<RegisterUserResult>>
            registerUserHandler(
                    UserRepository userRepository,
                    com.iyte_yazilim.proje_pazari.infrastructure.persistence
                                    .EmailVerificationRepository
                            emailVerificationRepository,
                    IValidator<RegisterUserCommand> validator,
                    RegisterUserMapper registerUserMapper,
                    com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper
                            userMapper,
                    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                    com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService
                            verificationTokenService,
                    org.springframework.context.ApplicationEventPublisher eventPublisher) {
        return new RegisterUserHandler(
                userRepository,
                emailVerificationRepository,
                validator,
                registerUserMapper,
                userMapper,
                passwordEncoder,
                verificationTokenService,
                eventPublisher);
    }

    @Bean
    public IRequestHandler<LoginUserCommand, ApiResponse<LoginUserResult>> loginUserHandler(
            UserRepository userRepository,
            com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository
                    emailVerificationRepository,
            IValidator<LoginUserCommand> validator,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil jwtUtil) {
        return new LoginUserHandler(
                userRepository, emailVerificationRepository, validator, passwordEncoder, jwtUtil);
    }

    @Bean
    public IRequestHandler<VerifyEmailCommand, ApiResponse<VerifyEmailResult>> verifyEmailHandler(
            com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository
                    emailVerificationRepository,
            UserRepository userRepository,
            com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService tokenService,
            com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper
                    userMapper) {
        return new VerifyEmailHandler(
                emailVerificationRepository, userRepository, tokenService, userMapper);
    }

    @Bean
    public IRequestHandler<ResendVerificationEmailCommand, ApiResponse<Void>>
            resendVerificationEmailHandler(
                    UserRepository userRepository,
                    com.iyte_yazilim.proje_pazari.infrastructure.persistence
                                    .EmailVerificationRepository
                            emailVerificationRepository,
                    com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService
                            tokenService,
                    org.springframework.context.ApplicationEventPublisher eventPublisher) {
        return new ResendVerificationEmailHandler(
                userRepository, emailVerificationRepository, tokenService, eventPublisher);
    }

    // ========== User Query Beans ==========
    @Bean
    public IRequestHandler<GetUserQuery, ApiResponse<UserDto>> getUserHandler(
            UserRepository userRepository,
            com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper userMapper,
            UserDtoMapper userDtoMapper) {
        return new GetUserHandler(userRepository, userMapper, userDtoMapper);
    }

    @Bean
    public IRequestHandler<GetAllUsersQuery, ApiResponse<List<UserDto>>> getAllUsersHandler(
            UserRepository userRepository,
            com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper userMapper,
            UserDtoMapper userDtoMapper) {
        return new GetAllUsersHandler(userRepository, userMapper, userDtoMapper);
    }
}
