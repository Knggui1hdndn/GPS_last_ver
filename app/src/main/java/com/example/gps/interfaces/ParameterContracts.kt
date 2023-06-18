package com.example.gps.interfaces

interface ParameterContracts {
    interface View {
        fun onShowMaxSpeed(string:String)
        fun onShowDistance(string:String)
        fun onShowAverageSpeed(string:String)
        fun onHideStart()
        fun onHideStop()
        fun onHideReset()
        fun onHideResume()
        fun onHidePause()
        fun onShowStart()
        fun onShowStop()
        fun onShowReset()
        fun onShowReset(int: Int)
        fun onShowResume()
        fun onShowPause()

    }

    interface Presenter {
        fun showReset()
        fun getMaxSpeed()
        fun getDistance()
        fun getAverageSpeed()
        fun startService()
        fun stopService()
        fun pauseService()
        fun resumeService()
        fun insertMovementDataWhenStart()
        fun setState(state: String)
        fun callMyService(action: String)
        fun isMyServiceRunning(serviceClass: Class<*>):Boolean
        fun updateUIState()
    }
}