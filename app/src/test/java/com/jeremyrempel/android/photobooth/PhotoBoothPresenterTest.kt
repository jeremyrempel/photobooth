package com.jeremyrempel.android.photobooth

import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class PhotoBoothPresenterTest {

    private lateinit var presenter: PhotoBoothPresenter
    private lateinit var view: PhotoBoothView

    @Before
    fun setup() {
        view = mockk(relaxed = true)
        presenter = PhotoBoothPresenter(view, mockk(relaxed = true))
    }

    @Test
    fun `presenter calls view undo`() {
        presenter.undo()
        verify { view.onUndo() }
    }

    @Test
    fun `presenter clear resets view and deletes file`() {
        presenter.clear()
        verify { view.onClear() }
    }

    @Test
    fun `take photo requests camera`() {
        presenter.takePhoto(mockk())
        verify { view.requestCamera(any()) }
    }
}