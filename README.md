# org.openhab.binding.sonypjtalk

This binding is meant for communicate with PJTalk compatible devices, mainly SONY video projector.

## Supported Things

Compatible SONY video projectors are supposed to be all supported by this binding. For the moment, i've only tested with a VPL-HW65ES. It could, at least, be compatible with the following :

* VPL-HW40ES
* VPL-HW55ES
* VPL-VW100

## Discovery

This binding has no auto-discovery feature.

## Thing Configuration

A video projector has to be configured with only 2 parameters :

* ip (hostname) : you need to configure where is the device ;
* community : this is a concept from SONY to isolate devices into groups. By default, community is SONY.

## Channels

A video projector has only on read / write channel : power. It's a switch that allow user to control if power has to be turned on or off, and to view if power is on or off.

3 more channels are available in read only mode :

* powerStatus : it's a more precise view on the power status (power on, cooling, power saving, standby, ...) ;
* lampTimer : lamp timer in hour ;
* modelName : the name of the model (for example : VPL-HW65ES).

## Compile

If you want to contribute, you'll need to :

* download the following project : https://github.com/openhab/openhab2-addons/tree/2.4.0 into openhab2-addons-2.4.0 directory
* download this project in the following directory : openhab2-addons-2.4.0\addons\binding\org.openhab.binding.sonypjtalk

The compile command is : mvn install

## Installation

Once the project compiled, or when you'll have downloaded the binary, you'll need to copy the jar file in the correct directory. The official explanation can be found here : https://www.openhab.org/docs/configuration/addons.html#through-manually-provided-add-ons. There are tons of forums that deals with problems that you could encountered. If needed, keyword you'll have to use in your search engine will be the following : openhab2 add binding manually.

## Any custom content here!

This binding is in very early stage of developpement. It totally fits my needs, but feel free to request more capabilities. Don't hesitate to request for help, it will be a pleasure !
