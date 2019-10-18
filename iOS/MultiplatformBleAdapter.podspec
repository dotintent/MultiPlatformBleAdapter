Pod::Spec.new do |spec|
  spec.name         = "MultiplatformBleAdapter"
  spec.version      = "0.0.1"
  spec.summary      = "An adapter for RxBluetoothKit that exposes consist API to crossplatform libraries"

  spec.description  = <<-DESC
  An adapter for RxBluetoothKit that exposes consist API to crossplatform libraries 
                  DESC

  spec.homepage     = "https://github.com/Polidea/MultiPlatformBleAdapter"
  spec.license      = "Apache License, Version 2.0."
  spec.author             = { "Pawel Scibek" => "pawel.scibek@polidea.com", "Tomasz Bogusz" => "tomasz.bogusz@polidea.com" }
  spec.social_media_url   = "https://twitter.com/polidea"

  spec.ios.deployment_target = "8.0"
  spec.osx.deployment_target = "10.10"

  spec.source       = { :git => "https://github.com/Polidea/MultiPlatformBleAdapter.git", :tag => "#{spec.version}" }


  spec.source_files  = "Classes", "Classes/**/*.{h,m}"
  spec.exclude_files = "Classes/Exclude"

  # spec.public_header_files = "Classes/**/*.h"

  spec.source_files = 'Source/*.swift'
  spec.osx.exclude_files = 'Source/RestoredState.swift', 'Source/CentralManager+RestoredState.swift', 'Source/CentralManagerRestoredState.swift'
  spec.frameworks   = 'CoreBluetooth'
  spec.dependency 'RxSwift', '~> 5.0'

  # ――― Project Linking ―――――――――――――――――――――――――――――――――――――――――――――――――――――――――― #
  #
  #  Link your library with frameworks, or libraries. Libraries do not include
  #  the lib prefix of their name.
  #

  # spec.framework  = "SomeFramework"
  # spec.frameworks = "SomeFramework", "AnotherFramework"

  # spec.library   = "iconv"
  # spec.libraries = "iconv", "xml2"

  spec.requires_arc = true


end
