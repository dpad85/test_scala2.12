package fr.acinq.calculator

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.acinq.calculator.databinding.MainActivityBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {

  private lateinit var mBinding: MainActivityBinding
  private lateinit var model: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    model = ViewModelProvider(this).get(MainViewModel::class.java)
    mBinding = DataBindingUtil.setContentView(this, R.layout.main_activity)
    mBinding.lifecycleOwner = this
    mBinding.model = model
  }

  override fun onStart() {
    super.onStart()
    mBinding.buttonAdd.setOnClickListener { model.add(1) }
    mBinding.buttonAdd.setOnLongClickListener {
      model.add(100)
      true
    }
    mBinding.buttonSubtract.setOnClickListener { model.subtract(1) }
    mBinding.buttonSubtract.setOnLongClickListener {
      model.subtract(100)
      true
    }
  }
}

class MainViewModel : ViewModel() {
  val display = MutableLiveData<Int>()

  private val system: ActorSystem
  private val actor: ActorRef

  init {
    display.value = 0
    system = ActorSystem.create("system")
    actor = system.actorOf(Props.create(Calculator::class.java), "calculator")

    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this)
    }
  }

  override fun onCleared() {
    EventBus.getDefault().unregister(this)
    system.terminate()
    super.onCleared()
  }

  fun add(x: Int) {
    actor.tell(Add(x), ActorRef.noSender())
  }

  fun subtract(x: Int) {
    actor.tell(Subtract(x), ActorRef.noSender())
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  fun handleCalculatorState(event: CalculatorPayload) {
    display.postValue(event.payload)
  }
}

