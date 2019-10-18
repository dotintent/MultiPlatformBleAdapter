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

  spec.source_files  = "iOS/classes", "iOS/classes/**/*.{h,m,swift}", "iOS/RxBluetoothKit", "iOS/RxBluetoothKit/**/*.{h,m,swift}", "iOS/RxSwift", "iOS/RxSwift/**/*.{h,m,swift}"

  spec.frameworks   = 'CoreBluetooth'

  spec.requires_arc = true

end
