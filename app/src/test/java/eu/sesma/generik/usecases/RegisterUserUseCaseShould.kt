package eu.sesma.generik.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import eu.sesma.generik.api.model.Login
import eu.sesma.generik.repository.NetworkResultCode
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.util.concurrent.TimeUnit


class RegisterUserUseCaseShould : BaseRepositoryUseCaseTest() {

    private lateinit var usecase: RegisterUserUseCase

    @Before
    override fun setUp() {
        super.setUp()
        usecase = RegisterUserUseCase(repository)
    }

    @Test
    fun callbackErrorWhenErrorInRegister() {
        val response = getResponse(404, true)
        doReturn(response).whenever(call).execute()
        doReturn(call).whenever(loginRegisterService).register(any())

        usecase.execute(Login(), 5)

        TimeUnit.MILLISECONDS.sleep(200);
        verify(networkResultLiveData).setNetworkResult(any())
        Assertions.assertThat(resultCaptor.firstValue.code).isEqualTo(NetworkResultCode.BAD_URL)
    }

    @Test
    fun createUserAccountOnDbOnSucessfulRegister() {
        val response = getResponse(200, false)
        doReturn(response).whenever(call).execute()
        doReturn(call).whenever(loginRegisterService).register(any())

        usecase.execute(Login(), 5)

        TimeUnit.MILLISECONDS.sleep(200);
        Assertions.assertThat(resultCaptor.firstValue.code).isEqualTo(NetworkResultCode.SUCCESS)
    }


    private fun getResponse(code: Int, error: Boolean): Response<Login> {
        if (error) {
            val responseBody = ResponseBody.create(MediaType.parse("application/json"), "{}")
            return Response.error<Login>(code, responseBody)
        }
        val login = Login()
        return Response.success(login)
    }
}