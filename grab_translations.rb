#!/usr/bin/env ruby

require 'net/http'
require 'uri'

require '~/src/ath/const.rb'

LANGS_URI = 'http://ath.darshancomputing.com/bi/langs/'

langs = Net::HTTP.get(URI.parse(LANGS_URI)).split()

langs.each do |lang|
  if lang.length == 2
    dir = 'res/values-' << lang
  else
    dir = 'res/values-' << lang[0,2] << '-r' << lang[2,2]
  end

  if ! Dir.exists?(dir)
    Dir.mkdir(dir)
  end

  fpath = dir + '/strings.xml'

  File.open(fpath, 'w') do |file|
    strings = Net::HTTP.get(URI.parse(LANGS_URI + lang))
    file.write(strings)
    file.sync # Want to be sure `file' command below sees new contents
  end

  puts `file #{fpath}`
end if false

lang_entries = ["System Selected", "English"]
lang_values  = ["default", "en"]

Const::Languages.each do |name, code|
  next if !langs.include?(code)

  code = code[0,2] << '_' << code[2,2] if code.length > 2

  lang_entries << name
  lang_values  << code
end

File.open('res/values/langs.xml', 'w') do |file|
  write_array = lambda do |name, ar|
    file.write(%Q{  <string-array name="#{name}">\n});
    ar.each do |item|
      file.write("    <item>#{item}</item>\n")
    end
    file.write(%Q{  </string-array>\n});
  end

  file.write(%Q{<?xml version="1.0" encoding="utf-8"?>
<!-- Generated by grab_translations.rb -->
<resources>
});

  write_array.call("lang_entries", lang_entries)
  write_array.call("lang_values",  lang_values)

  file.write(%Q{</resources>\n})
end
