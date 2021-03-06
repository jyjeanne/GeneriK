package eu.sesma.generik.ui.pin

import android.os.Handler
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import eu.sesma.generik.R
import eu.sesma.generik.ui.BaseActivity
import javax.inject.Inject


class PinDecorator
@Inject constructor(
        private val activity: BaseActivity
) : PinUserInterface {

    companion object {
        private val CODE_LEN = 4
    }

    @BindView(R.id.tv_code)
    lateinit var pin: TextView
    @BindView(R.id.tv_fpexplanation)
    lateinit var fpText: TextView
    @BindView(R.id.im_fpicon)
    lateinit var fpIcon: ImageView

    private var codeText = ""

    private var delegate: PinUserInterface.Delegate? = null

    fun bind(view: View) {
        ButterKnife.bind(this, view)
        initToolbar()
    }

    fun dispose() {
        delegate = null
    }

    override fun initialize(delegate: PinUserInterface.Delegate) {
        this.delegate = delegate
    }

    override fun showFingerprint(show: Boolean){
        fpIcon.visibility= if (show) VISIBLE else GONE
        fpText.visibility= if (show) VISIBLE else GONE
    }

    private fun initToolbar() {
        val actionBar = activity.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
    }

    @OnClick(R.id.bt_0, R.id.bt_1, R.id.bt_2, R.id.bt_3, R.id.bt_4,
            R.id.bt_5, R.id.bt_6, R.id.bt_7, R.id.bt_8, R.id.bt_9)
    fun onNumberClick(button: Button) {
        if (codeText.length >= CODE_LEN * 2) return
        when (button.id) {
            R.id.bt_0 -> codeText += "0 "
            R.id.bt_1 -> codeText += "1 "
            R.id.bt_2 -> codeText += "2 "
            R.id.bt_3 -> codeText += "3 "
            R.id.bt_4 -> codeText += "4 "
            R.id.bt_5 -> codeText += "5 "
            R.id.bt_6 -> codeText += "6 "
            R.id.bt_7 -> codeText += "7 "
            R.id.bt_8 -> codeText += "8 "
            R.id.bt_9 -> codeText += "9 "
        }
        pin.text = codeText
        if (codeText.length == CODE_LEN * 2) sendCode()
    }

    private fun sendCode() {
        val rawCode = codeText.filter { it != ' ' }
        Handler().postDelayed(Runnable {
            delegate?.onCode(rawCode)
        }, 250)
    }

    @OnClick(R.id.bt_back)
    fun onBackClick() {
        codeText = codeText.dropLast(2)
        pin.text = codeText
    }
}