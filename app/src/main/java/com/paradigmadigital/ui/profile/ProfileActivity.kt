package com.paradigmadigital.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.credentials.Credential
import com.paradigmadigital.R
import com.paradigmadigital.ui.BaseActivity
import com.paradigmadigital.ui.register.RegisterDecorator.Companion.RESOLVE_HINT
import javax.inject.Inject


class ProfileActivity : BaseActivity() {

    @Inject
    lateinit var decorator: ProfileDecorator
    @Inject
    lateinit var presenter: ProfilePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        activityComponent.inject(this)

        decorator.bind(getRootView())
        presenter.initialize(decorator, resultViewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        decorator.dispose()
        presenter.dispose()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT && resultCode == Activity.RESULT_OK) {
            val credential: Credential = data.getParcelableExtra(Credential.EXTRA_KEY)
            presenter.setPhone(credential.id)
        }
    }
}