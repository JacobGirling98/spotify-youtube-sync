package org.example.http.auth

import org.http4k.lens.FormField
import org.http4k.lens.string


val grantTypeField = FormField.string().required("grant_type")
