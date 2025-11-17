package com.bbt.ghostroom

import io.reactivex.disposables.CompositeDisposable

interface Base {
    val disposables:CompositeDisposable
}