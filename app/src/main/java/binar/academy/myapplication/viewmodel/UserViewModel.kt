package binar.academy.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import binar.academy.myapplication.model.UserResponse
import binar.academy.myapplication.network.RetrofitUser
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel : ViewModel() {
    private val lvUser = MutableLiveData<UserResponse>()
    val registeredUser : LiveData<UserResponse> = lvUser

    fun postAPIUser(name : RequestBody, email : RequestBody, pass : RequestBody, phone : RequestBody, address : RequestBody, city : RequestBody, image : MultipartBody.Part) {

        val userClient = RetrofitUser.userCall().postUser(name, email, pass, phone, address, city, image)

        userClient.enqueue(object : Callback<UserResponse> {

            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        lvUser.postValue(data)
                    } else {
                        lvUser.postValue(null)
                    }
                } else {
                    lvUser.postValue(null)
                    Log.d("failed", response.message())
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.d("failed", t.message!!)
                lvUser.postValue(null)
            }
        })
    }
}