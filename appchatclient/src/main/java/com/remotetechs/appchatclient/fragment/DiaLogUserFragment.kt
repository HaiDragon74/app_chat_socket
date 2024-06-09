package com.remotetechs.appchatclient.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.remotetechs.appchatclient.R
import com.remotetechs.appchatclient.model.Message

class DiaLogUserFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_dia_log_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appCompatButtonLogin = view.findViewById<AppCompatButton>(R.id.btn_login)
        val edtIpServer = view.findViewById<EditText>(R.id.edt_ip_server)
        val edtName = view.findViewById<EditText>(R.id.edt_name)
        isCancelable = false
        edtName.requestFocus()
        appCompatButtonLogin.setOnClickListener {
            val nameUse = edtName.text.toString()
            val ipServer = edtIpServer.text.toString()
            Message(name = nameUse)
            findNavController().navigate(
                R.id.action_diaLogUserFragment_to_messageFragment,
                bundleOf(
                    "name_user" to nameUse,
                    "ip_server" to ipServer
                )
            )
            dismiss()
        }
    }

}