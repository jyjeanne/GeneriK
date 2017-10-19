package com.paradigmadigital.repository

import android.arch.lifecycle.LiveData
import android.os.SystemClock
import com.paradigmadigital.api.services.LoginRegisterService
import com.paradigmadigital.domain.db.UserDao
import com.paradigmadigital.domain.entities.User
import com.paradigmadigital.platform.CallbackFun
import com.paradigmadigital.repository.NetworkResultCode.*
import retrofit2.Retrofit
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject


class Repository
@Inject
constructor(
        private val preferences: Preferences,
        private val networkResultLiveData: NetworkResultLiveData,
        private val userDao: UserDao,
        private val securePreferences: SecurePreferences,
        private val retrofit: Retrofit,
        private val executor: Executor
) {
    val loginRegisterService = retrofit.create(LoginRegisterService::class.java)

    fun getErrors(): LiveData<NetworkResult> = networkResultLiveData

    fun isLoggedIn() = preferences.isloggedIn

    fun setLoggedIn(logged: Boolean) {
        preferences.isloggedIn = logged
        if (!logged) securePreferences.password = ""
    }

    fun getUser(): LiveData<User> {
        return userDao.get()
    }

    fun getPass() = securePreferences.password

    fun setUser(user: User, pass: String) {
        sendUserData(user, pass) { networkResultLiveData.setNetworkResult(it) }
    }

    fun setUser(email: String) {
        userDao.insert(User("", "", Date(0), "", email))
    }

    fun requestCode(id: Int) {
        requestCode(userDao.getEmail(), id) { networkResultLiveData.setNetworkResult(it) }
    }

    fun setPass(id: Int) {
        sendUserPass(userDao.getEmail(), getPass(), id) { networkResultLiveData.setNetworkResult(it) }
    }

    fun login(email: String, pass: String) {
        setLoggedIn(false)
        sendLogin(email, pass) { networkResultLiveData.setNetworkResult(it) }
    }

    private fun sendUserData(user: User, pass: String, callback: CallbackFun<NetworkResult>) {
        executor.execute {
            try {
                SystemClock.sleep(1000) //TODO: call register service and get reurned uid
//                val body = loginRegisterService.sendUserData(user).execute().body()
//                if ( body == null) throw UnknownHostException()
//                userDao.insert(user.copy(uid = body.uid))
                userDao.insert(user.copy(uid = ""))
                securePreferences.password = pass
                callback(NetworkResult(SUCCESS, 0))
            } catch (e: Throwable) {
                manageExceptions(e, callback, 0)
            }
        }
    }

    private fun requestCode(uid: String, id: Int, callback: CallbackFun<NetworkResult>) {
        executor.execute {
            try {
                SystemClock.sleep(1000) //TODO: call set pass service
//                if (loginRegisterService.requestCode(uid).execute().body() == null) throw UnknownHostException()
                callback(NetworkResult(SUCCESS, id))
            } catch (e: Throwable) {
                manageExceptions(e, callback, id)
            }
        }
    }

    private fun sendUserPass(uid: String, pass: String, id: Int, callback: CallbackFun<NetworkResult>) {
        executor.execute {
            try {
                SystemClock.sleep(1000) //TODO: call request code service
//                if (loginRegisterService.sendUserPass(uid, pass).execute().body() == null) throw UnknownHostException()
                callback(NetworkResult(SUCCESS, id))
            } catch (e: Throwable) {
                securePreferences.password = ""
                manageExceptions(e, callback, id)
            }
        }
    }

    private fun sendLogin(email: String, pass: String, callback: CallbackFun<NetworkResult>) {
        executor.execute {
            try {
                SystemClock.sleep(1000) //TODO: call login service
//                if (loginRegisterService.sendLogin(email, pass).execute().body() == null) throw UnknownHostException()
                val a = userDao.getEmail()
                val b = securePreferences.password
                if (email == userDao.getEmail() && pass == securePreferences.password) {
                    setLoggedIn(true)
                }
                callback(NetworkResult(SUCCESS, 0))
            } catch (e: Throwable) {
                securePreferences.password = ""
                manageExceptions(e, callback, 0)
            }
        }
    }

    private fun manageExceptions(e: Throwable, callback: CallbackFun<NetworkResult>, id: Int) {
        when (e) {
            is UnknownHostException -> callback(NetworkResult(DISCONNECTED, id))
            is IllegalArgumentException -> callback(NetworkResult(BAD_URL, id))
            else -> callback(NetworkResult(UNKNOWN, id))
        }
    }
}