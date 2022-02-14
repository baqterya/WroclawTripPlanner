package com.baqterya.wroclawtripplanner.view.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baqterya.wroclawtripplanner.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse?) {}

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse?) {
                    if (permissionDeniedResponse?.isPermanentlyDenied == true) {
                        val permissionAlert = MaterialAlertDialogBuilder(this@LoginActivity)
                            .setTitle("Location permission denied")
                            .setMessage("This app needs.\n" +
                                    "Do you want to go to system settings to allow the permission?")
                            .setPositiveButton("OK") { _, _ ->
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                intent.data = Uri.fromParts("package", packageName, null)
                                startActivity(intent)
                            }
                            .setNegativeButton("No", null)
                        permissionAlert.show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Permission denied.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissionRequest: PermissionRequest?, permissionToken: PermissionToken?) {}
            })
            .check()
    }
}