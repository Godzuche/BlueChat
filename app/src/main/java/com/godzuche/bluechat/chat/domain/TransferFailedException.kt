package com.godzuche.bluechat.chat.domain

import java.io.IOException

class TransferFailedException: IOException("Reading incoming data failed")