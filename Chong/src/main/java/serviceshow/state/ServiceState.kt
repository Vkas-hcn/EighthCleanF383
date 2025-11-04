package serviceshow.state

import com.september.nine.chong.data.KeyCon


object ServiceState {
    

    fun markServiceStarted() {
        KeyCon.isOpenNotification = true
    }
    

    fun markServiceStopped() {
        KeyCon.isOpenNotification = false
    }

}

