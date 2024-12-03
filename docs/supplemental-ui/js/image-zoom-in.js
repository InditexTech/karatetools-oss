/* eslint-disable no-undef */
/* eslint-disable quotes */
'use strict'
; (function () {
  $(document).ready(() => {
    $("div.imageblock.zoom-in").on("click tap", function () {
      const imageEle = $(this).find("img")
      if (imageEle) {
        const imageContainer = $("<div>")
        imageContainer.addClass("imageblock zoomed-in")
        const imageContainerShadowTop = $("<div>")
        imageContainerShadowTop.addClass("zoomed-in-top-content")
        const imageClose = $("<div>")
        imageClose.addClass("zoomed-in-close")
        imageContainer.on("click tap", function () {
          imageContainer.remove()
        })
        const imageCloseIcon = $("<svg data-icon-name=\"close-large\" width=\"1em\" height=\"1em\" viewBox=\"0 0 20 20\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\" class=\"sw-icon sw-icon--monochrome sw-lightbox__close-button\"><mask id=\"icon-close-large_svg__a115\" maskUnits=\"userSpaceOnUse\" x=\"1\" y=\"1\" width=\"18\" height=\"18\" style=\"mask-type: alpha;\"><path d=\"m10 10.707 7.646 7.647.708-.707L10.707 10l7.647-7.646-.707-.708L10 9.293 2.354 1.646l-.708.708L9.293 10l-7.647 7.646.708.708L10 10.707Z\" fill=\"#FF8000\"></path></mask><g mask=\"url(#icon-close-large_svg__a115)\"><path fill=\"#FF8000\" d=\"M0 0h20v20H0z\"></path></g></svg>")
        imageCloseIcon.on("click tap", function () {
          imageContainer.remove()
        })
        imageClose.append(imageCloseIcon)
        const imageZoomed = imageEle.clone()
        imageZoomed.removeAttr("width")
        imageZoomed.addClass("zoomed-in-image")
        imageContainerShadowTop.append(imageClose)
        imageContainer.append(imageContainerShadowTop)
        imageContainer.append(imageZoomed)
        $("body").append(imageContainer)
      }
    })
  })
})()
