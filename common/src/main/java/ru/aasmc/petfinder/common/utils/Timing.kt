package ru.aasmc.petfinder.common.utils

class Timing {
    companion object {
        fun doRandomWork() {
            val number = (0..100).random()
            factorial(number)
        }

        private tailrec fun factorial(number: Int, accumulator: Int = 1): Int {
            return when (number) {
                1 -> accumulator
                else -> factorial(number - 1, accumulator * number)
            }
        }
    }

}