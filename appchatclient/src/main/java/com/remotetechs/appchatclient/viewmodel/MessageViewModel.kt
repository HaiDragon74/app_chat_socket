package com.remotetechs.appchatclient.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.remotetechs.appchatclient.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MessageViewModelFactory(private val repository: Repository):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessageViewModel(repository) as T

    }
}

class MessageViewModel(private val repository: Repository):ViewModel() {
    private val _liveDataMessage=MutableLiveData<List<Message>>()
    val liveData:LiveData<List<Message>> =_liveDataMessage
    private val _liveDataNameUser=MutableLiveData<String>()
    val liveDataNameUser:LiveData<String> =_liveDataNameUser
    private val _liveDataName=MutableLiveData<String>()
    val liveDataName:LiveData<String> =_liveDataName

    fun insertMessage(message: Message){
        viewModelScope.launch(Dispatchers.IO){
            repository.insertMessage(message)
        }
    }
    fun realMessage(){
        viewModelScope.launch{
            repository.realMessage().onEach {listMesage->
                _liveDataMessage.value=listMesage
            }.launchIn(viewModelScope)
        }
    }
    fun setNameUser(name: String){
        _liveDataNameUser.value=name
    }
    fun checkName(name: String){
        viewModelScope.launch(Dispatchers.Main){
            _liveDataName.value=name
        }
    }

}