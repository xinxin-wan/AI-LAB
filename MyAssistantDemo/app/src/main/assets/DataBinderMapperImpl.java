package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new com.intel.automotive.ai.phonon.DataBinderMapperImpl());
  }
}
