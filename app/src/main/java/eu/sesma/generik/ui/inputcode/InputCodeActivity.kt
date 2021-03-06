package eu.sesma.generik.ui.inputcode

import android.os.Bundle
import eu.sesma.generik.R
import eu.sesma.generik.navigation.Navigator
import eu.sesma.generik.ui.BaseActivity
import javax.inject.Inject

class InputCodeActivity : BaseActivity() {

    @Inject
    lateinit var decorator: InputCodeDecorator
    @Inject
    lateinit var presenter: InputCodePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_code)
        activityComponent.inject(this)

        decorator.bind(getRootView())
        val email = intent.extras.getString(Navigator.EXTRA_EMAIL)
        val pass = intent.extras.getString(Navigator.EXTRA_PASS)
        presenter.initialize(decorator, resultViewModel, email, pass)
    }


    override fun onDestroy() {
        super.onDestroy()
        decorator.dispose()
        presenter.dispose()
    }
}