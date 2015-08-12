# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  # we are starting from a custom box "package.box"
  # you can add this box to vagrant
  # with:
  # vagrant box add package.box

  config.vm.box = "ubuntu/trusty64_oraclejava8"

  config.vm.synced_folder ".", "/vagrant"
  config.vm.provider "virtualbox" do |v|
      v.memory = 2048
  end

  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
    config.cache.enable :apt
  end



  config.vm.define "seed" do |seed|
     seed.vm.network "private_network", ip: "192.168.11.20"
   end



  (2..4).each do |i|
     config.vm.define "member_#{i}" do |member|
        member.vm.network "private_network", ip: "192.168.11.2#{i}"
      end
  end
end
