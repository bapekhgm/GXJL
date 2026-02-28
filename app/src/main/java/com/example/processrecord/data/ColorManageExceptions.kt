package com.example.processrecord.data

class ColorPresetNameConflictException(val presetName: String) : IllegalStateException()
class ColorGroupNameConflictException(val groupName: String) : IllegalStateException()
