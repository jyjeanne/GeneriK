package eu.sesma.generik.repository

import android.arch.lifecycle.LiveData
import com.nhaarman.mockito_kotlin.*
import eu.sesma.generik.account.OauthAccountManager
import eu.sesma.generik.api.model.Login
import eu.sesma.generik.api.services.LoginRegisterService
import eu.sesma.generik.domain.db.UserDao
import eu.sesma.generik.domain.entities.User
import eu.sesma.generik.domain.mappers.UserMapper
import eu.sesma.generik.repository.preferences.Preferences
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit


class LoginRepositoryShould {
    @Mock
    private lateinit var networkResultLiveData: NetworkResultLiveData
    @Mock
    private lateinit var userDao: UserDao
    @Mock
    private lateinit var preferences: Preferences
    @Mock
    private lateinit var userMapper: UserMapper
    @Mock
    private lateinit var accountManager: OauthAccountManager
    @Mock
    private lateinit var retrofit: Retrofit
    @Mock
    private lateinit var loginRegisterService: LoginRegisterService

    private val resultCaptor = argumentCaptor<NetworkResult>()
    private lateinit var repository: LoginRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(retrofit.create<LoginRegisterService>(any())).thenReturn(loginRegisterService)
        doNothing().whenever(networkResultLiveData).setNetworkResult(resultCaptor.capture())
        repository = LoginRepository(
                networkResultLiveData,
                userDao,
                preferences,
                accountManager,
                userMapper,
                retrofit)
    }

    @Test
    fun returnNetworkLiveDataOnGetErrors() {

        val errorLiveData = repository.getErrors()

        assertThat(errorLiveData).isEqualTo(networkResultLiveData)
    }

    @Test
    fun returnFalseOnIsLoggedInWhenTokenNotExist() {
        whenever(accountManager.isLoggedIn()).thenReturn(false)

        val logged = repository.isLoggedIn()

        verify(accountManager).isLoggedIn()
        assertThat(logged).isFalse()
    }

    @Test
    fun returnTrueOnIsLoggedInWhenTokenExist() {
        whenever(accountManager.isLoggedIn()).thenReturn(true)

        val logged = repository.isLoggedIn()

        verify(accountManager).isLoggedIn()
        assertThat(logged).isTrue()
    }


    @Test
    fun getEmailfromAccountManagerOnGetEmail() {
        whenever(accountManager.getEmail()).thenReturn("bob@acme.com")

        val email = repository.getEmail()

        assertThat(email).isEqualTo("bob@acme.com")
    }


    @Test
    fun getUserliveDataOnGetUser() {
        val liveData = object : LiveData<User>() {}
        whenever(userDao.get()).thenReturn(liveData)

        val userliveData = repository.getUserLiveData()

        verify(userDao).get()
        assertThat(userliveData).isEqualTo(liveData)
    }

    @Test
    fun setUserOnSetUser() {
        val loginCaptor = argumentCaptor<Login>()
        doReturn(User(email = "bob@acme.com")).whenever(userMapper).map(loginCaptor.capture())

        repository.setUser(Login(email = "bob@acme.com"))

        verify(userMapper).map(any())
        verify(userDao).insert(any())
        assertThat(loginCaptor.firstValue.email).isEqualTo("bob@acme.com")
    }

    @Test
    fun setCodeOnSetCode() {
        val date = Date()

        repository.setCode("123456", date, "bob@acme.com")

        verify(preferences).setCode("123456", date, "bob@acme.com")
    }


    @Test
    fun getCodeOngetCodeOnTimeForCorrectUser() {
        whenever(preferences.codeTime).thenReturn(Date())
        whenever(preferences.codeEmail).thenReturn("bob@acme.com")
        whenever(preferences.code).thenReturn("123456")

        val code = repository.getCode("bob@acme.com")

        assertThat(code).isEqualTo("123456")
    }


    @Test
    fun doNotGetCodeOngetCodeOffTimeForCorrectUser() {
        whenever(preferences.codeTime).thenReturn(Date(0))
        whenever(preferences.codeEmail).thenReturn("bob@acme.com")
        whenever(preferences.code).thenReturn("123456")

        val code = repository.getCode("bob@acme.com")

        assertThat(code).isEqualTo("")
    }


    @Test
    fun doNotGetCodeOngetCodeOonTimeForIncorrectUser() {
        whenever(preferences.codeTime).thenReturn(Date())
        whenever(preferences.codeEmail).thenReturn("ann@acme.com")
        whenever(preferences.code).thenReturn("123456")

        val code = repository.getCode("bob@acme.com")

        assertThat(code).isEqualTo("")
    }


    @Test
    fun sendDisconnectedOnUnknownHostException() {

        repository.executeInteractor {
            throw UnknownHostException()
        }

        TimeUnit.MILLISECONDS.sleep(200);
        assertThat(resultCaptor.firstValue.code).isEqualTo(NetworkResultCode.DISCONNECTED)
    }

    @Test
    fun sendBadUrlOn404() {

        repository.executeInteractor {
            throw HttpException(Response.error<String>(
                    404,
                    ResponseBody.create(MediaType.parse("application/json"), "{}")))
        }

        TimeUnit.MILLISECONDS.sleep(200);
        assertThat(resultCaptor.firstValue.code).isEqualTo(NetworkResultCode.BAD_URL)
    }

    @Test
    fun sendUnknownOnUnknownException() {

        repository.executeInteractor {
            throw RuntimeException()
        }

        TimeUnit.MILLISECONDS.sleep(200);
        assertThat(resultCaptor.firstValue.code).isEqualTo(NetworkResultCode.UNKNOWN)
    }

    @Test
    fun sendForbiddenOnForbidden403() {

        repository.executeInteractor {
            throw HttpException(Response.error<String>(
                    403,
                    ResponseBody.create(MediaType.parse("application/json"), "{}")))
        }

        TimeUnit.MILLISECONDS.sleep(200);
        assertThat(resultCaptor.firstValue.code).isEqualTo(NetworkResultCode.FORBIDDEN)
    }
}