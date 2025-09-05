add_library(text_utils STATIC text_string.cc)

target_link_libraries(text_utils PUBLIC fst)
