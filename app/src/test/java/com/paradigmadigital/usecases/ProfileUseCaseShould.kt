package com.paradigmadigital.usecases

import com.nhaarman.mockito_kotlin.verify
import com.paradigmadigital.api.model.Login
import com.paradigmadigital.domain.db.AuthorDao
import com.paradigmadigital.domain.db.PostDao
import com.paradigmadigital.domain.mappers.AuthorMapper
import com.paradigmadigital.domain.mappers.PostMapper
import com.paradigmadigital.repository.*
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ProfileUseCaseShould : MockWebServerTestBase() {

    lateinit private var useCase: ProfileUseCase
    @Mock lateinit var loginRepository: LoginRepository
    @Mock lateinit var postDao: PostDao
    @Mock lateinit var authorDao: AuthorDao

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        MockitoAnnotations.initMocks(this)
        val repository = DataRepository(postDao, authorDao, retrofit, AuthorMapper(), PostMapper())
        useCase = ProfileUseCase(repository, loginRepository)
    }

    @Test
    fun getCommentsHappyPath() {
        enqueueMockResponse(200, "login.json")
        val testObserver = TestObserver<ApiResult>()
        val login = Login(
                        token = "6432871b8c7ynkqwefjgjcbe89neqbfyqug",
                        uid = "12376",
                        name = "Bob",
                        phone = "+54123456789",
                        email = "bob@acme.com"
                )
        val result = login.toApiResult()

        useCase.execute(login).subscribe(testObserver)
        testObserver.await()

        testObserver.assertNoErrors()
                .assertValue(result)
        verify(loginRepository).setUser(login)
    }

    @Test
    fun getCommentsUsesCorrectUrl() {
        enqueueMockResponse(200, "login.json")

        useCase.execute(Login()).subscribe()

        assertPostRequestSentTo("/update")
    }

    @Test
    fun getCommentsManagesHttpError() {
        enqueueMockResponse(403, "login.json")
        val testObserver = TestObserver<ApiResult>()
        val result = ApiResult.Failure(NetworkResultCode.FORBIDDEN)

        useCase.execute(Login()).subscribe(testObserver)
        testObserver.await()

        testObserver.assertNoErrors()
                .assertValue(result)
    }
}
