/*
 * Copyright 2023 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.wallet.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.samples.wallet.R
import com.google.android.gms.samples.wallet.util.PaymentsUtil
import com.google.android.gms.samples.wallet.viewmodel.CheckoutViewModel
import com.google.android.gms.wallet.PaymentData
import com.google.pay.button.PayButton
import com.google.wallet.button.WalletButton

@Composable
fun ProductScreen(
    title: String,
    description: String,
    price: String,
    image: Int,
    viewModel: CheckoutViewModel,
    googleWalletButtonOnClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val padding = 20.dp
    val black = Color(0xff000000.toInt())
    val grey = Color(0xffeeeeee.toInt())

    val resolvePaymentForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        when (result.resultCode) {
            ComponentActivity.RESULT_OK -> result.data?.let { intent ->
                PaymentData.getFromIntent(intent)?.let(viewModel::setPaymentDataResult)
            }
            /* Handle other result scenarios
             * Learn more at: https://developers.google.com/pay/api/android/support/troubleshooting
             */
            else -> { // Other uncaught errors }
            }
        }
    }

    // Start the resolution of the task if needed
    state.paymentDataResolution?.let {
        resolvePaymentForResult.launch(IntentSenderRequest.Builder(it).build())
    }

    val payResult = state.paymentResult
    if (payResult != null) {
        Column(
            modifier = Modifier
                .testTag("successScreen")
                .background(grey)
                .padding(padding)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                contentDescription = null,
                painter = painterResource(R.drawable.check_circle),
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
            )
            Text(text = "${payResult.billingName} completed the payment.\nWe are preparing your order.")
        }

    } else {
        Column(
            modifier = Modifier
                .background(grey)
                .padding(padding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(space = padding / 2),
        ) {
            Image(
                contentDescription = null,
                painter = painterResource(image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            )
            Text(
                text = title,
                color = black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = price, color = black)
            Spacer(Modifier)
            Text(
                text = "Description",
                color = black,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = black
            )
            if (state.googlePayAvailable == true) {
                PayButton(
                    modifier = Modifier
                        .testTag("payButton")
                        .fillMaxWidth(),
                    onClick = { if (state.googlePayButtonClickable) viewModel.requestPayment() },
                    allowedPaymentMethods = PaymentsUtil.allowedPaymentMethods.toString()
                )
            }
            if (state.googleWalletAvailable == true) {
                Spacer(Modifier)
                Text(
                    text = "Or add a pass to your Google Wallet:",
                    color = black
                )
                WalletButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { if (state.googleWalletButtonClickable) googleWalletButtonOnClick() },
                )
            }
        }
    }
}
