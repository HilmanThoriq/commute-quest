package com.example.commutequest.api;

import com.example.commutequest.ResetPasswordFragment;
import com.example.commutequest.model.AuthResponse;
import com.example.commutequest.model.MapsLinkRequest;
import com.example.commutequest.model.MapsLinkResponse;
import com.example.commutequest.model.ProfileResponse;
import com.example.commutequest.model.LoginRequest;
import com.example.commutequest.model.RegisterRequest;
import com.example.commutequest.model.UpdateProfileRequest;
import com.example.commutequest.model.UpdateProfileResponse;
import com.example.commutequest.model.ForgotPasswordRequest;
import com.example.commutequest.model.ForgotPasswordResponse;
import com.example.commutequest.model.ChangePasswordRequest;
import com.example.commutequest.model.ChangePasswordResponse;
import com.example.commutequest.model.ResetPasswordRequest;
import com.example.commutequest.model.ResetPasswordResponse;
import com.example.commutequest.model.AutocompleteResponse;
import com.example.commutequest.model.RouteRequest;
import com.example.commutequest.model.RouteResponse;
import com.example.commutequest.HomeFragment;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface APIService {
    @POST("login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("register")
    Call<AuthResponse> register(@Body RegisterRequest registerRequest);

    @GET("profile")
    Call<ProfileResponse> getUserProfile(@Header("Authorization") String authToken);

    @PUT("profile")
    Call<UpdateProfileResponse> updateProfile(@Header("Authorization") String authHeader, @Body UpdateProfileRequest request);

    @POST("forgot-password")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("reset-password")
    Call<ResetPasswordResponse> resetPassword(@Body ResetPasswordRequest request);

    @POST("reset-password")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);

    Call<ResetPasswordFragment.ChangePasswordResponse> changePassword(String authHeader, ResetPasswordFragment.ChangePasswordRequest request);

    // Add autocomplete endpoint
    @POST("auto-complete")
    Call<AutocompleteResponse> getAutocompletePlaces(@Header("Authorization") String authHeader, @Body HomeFragment.AutocompleteRequest request);

    // Add route endpoint
    @POST("get-route")
    Call<RouteResponse> getRoute(@Header("Authorization") String authHeader, @Body RouteRequest request);

    @POST("get-maps-link")
    Call<MapsLinkResponse> getMapsLink(@Header("Authorization") String authHeader, @Body MapsLinkRequest request);

}