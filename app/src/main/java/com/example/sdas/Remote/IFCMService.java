package com.example.sdas.Remote;


import com.example.sdas.Model.MyResponse;
import com.example.sdas.Model.Request;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content_Type:application/json",
            "Authorization:key=AAAAgljvOtY:APA91bFxGhiTI3J_d_goYh1ddmHLDeyizTRurv9FJn0TK33lf7_Uio31gGka7F-2CfPOjlCsLBegvv4Nw4PNMkOJmSfCSbsnPxvvyt9nJ5RGPHOgGsatKpr-bqWH_-ie35YyqWce2NFy"

    })
    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser(@Body Request body);
}
