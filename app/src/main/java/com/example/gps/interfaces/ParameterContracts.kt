package com.example.gps.interfaces

interface ParameterContracts {
    interface View {
        fun showMaxSpeed(string:String)
        fun showDistance(string:String)
        fun showAverageSpeed(string:String)
        fun hideStart()
        fun hideStop()
        fun hideReset()
        fun hideResume()
        fun hidePause()
        fun showStart()
        fun showStop()
        fun showReset()
        fun showResume()
        fun showPause()
    }

    interface Presenter {
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