package com.wipro.auth_service.controller;

import com.wipro.auth_service.dto.APIResponse;
import com.wipro.auth_service.dto.ResetPasswordAPIRequest;
import com.wipro.auth_service.dto.UserData;
import com.wipro.auth_service.exception.UserNotFoundException;
import com.wipro.auth_service.service.AuthService;
import com.wipro.auth_service.service.RoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    AuthService userService;

    @Autowired
    RoleService roleService;

    @GetMapping
    public ResponseEntity<APIResponse<?>> getUserByIdOrUsername(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String username) {
        try {
            if (StringUtils.isBlank(id) && StringUtils.isBlank(username)) {
                APIResponse<?> badRequestResponse = new APIResponse<>(
                        HttpStatus.BAD_REQUEST,
                        "Either id or username must be provided",
                        null);
                return ResponseEntity.badRequest().body(badRequestResponse);
            }
            if(StringUtils.isNotBlank(id)){
                UserData user = userService.findById(id);
                APIResponse<UserData> userAPIResponse = new APIResponse<>(HttpStatus.OK, user);
                return ResponseEntity.ok(userAPIResponse);
            }
            if(StringUtils.isNotBlank(username)){
                UserData user = userService.findByUsername(username);
                APIResponse<UserData> userAPIResponse = new APIResponse<>(HttpStatus.OK, user);
                return ResponseEntity.ok(userAPIResponse);
            }
            APIResponse<?> userNotAPIResponse = new APIResponse<>(HttpStatus.NOT_FOUND, "User not found.", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(userNotAPIResponse);
        } catch (UserNotFoundException e) {
            APIResponse<?> userNotAPIResponse = new APIResponse<>(HttpStatus.NOT_FOUND, "User not found.", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(userNotAPIResponse);
        }
    }

    @PutMapping
    public ResponseEntity<APIResponse<?>> updateUser(@RequestParam String username,
            @RequestBody UserData updatedUserData){
        try{
            UserData updatedUser = userService.updateByUsername(username, updatedUserData);
            APIResponse<UserData> response = new APIResponse<>(HttpStatus.OK, updatedUserData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            APIResponse<?> userNotAPIResponse = new APIResponse<>(HttpStatus.BAD_REQUEST,e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userNotAPIResponse);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<APIResponse<?>> changePassword(@RequestParam String username,
                                                         @RequestBody ResetPasswordAPIRequest changePasswordRequest){
        try{
            userService.changePassword(username,changePasswordRequest.getCurrentPassword(),changePasswordRequest.getNewPassword());
            APIResponse<String> response=new APIResponse<>(HttpStatus.OK,"Password changed successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(HttpStatus.BAD_REQUEST,e.getMessage(),null));
        }
    }

    @DeleteMapping
    public ResponseEntity<APIResponse<?>> deleteUser(@RequestParam String username){
        try{
            userService.deleteByUserName(username);
            APIResponse<String> response=new APIResponse<>(HttpStatus.OK,"User deleted successfully.");
            return ResponseEntity.ok(response);
        }catch (Exception e){
            APIResponse<?> notFound=new APIResponse<>(HttpStatus.BAD_REQUEST,e.getMessage(),null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(notFound);
        }
    }

    @GetMapping("/check")
    public boolean hasPermission(@RequestParam String action) {
        return roleService.hasPermission(action);
    }
}
