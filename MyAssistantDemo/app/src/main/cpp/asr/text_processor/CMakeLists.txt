add_library(text_processor STATIC
        text_processor.cc
        text_token_parser.cc
)
if(ANDROID)
  target_link_libraries(text_processor PUBLIC fst text_utils)
else()
  if(MSVC)
    target_link_libraries(text_processor PUBLIC fst text_utils)
  else()
    target_link_libraries(text_processor PUBLIC dl fst text_utils)
  endif()
endif()

# ----------------------------------------------------------------------------
# C API shared library (wetext_processor_c)
# ----------------------------------------------------------------------------
add_library(text_processor_c SHARED text_processor_c_api.cc)

target_link_libraries(text_processor_c PUBLIC text_processor)
