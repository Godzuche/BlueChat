package com.example.bluechat.domain

import java.io.IOException

class TransferFailedException: IOException("Reading incoming data failed")