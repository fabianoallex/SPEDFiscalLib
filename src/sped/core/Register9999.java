package sped.core;

public class Register9999 extends NamedRegister {
    public static final String REGISTER_NAME = "9999";
    public static final String FIELD_REGISTER_COUNT_NAME = "QTD_LIN";
    private final Field<Integer> fieldRegisterCount;

    Register9999(Context context) {
        super(context, REGISTER_NAME);
        try {
            fieldRegisterCount = this.getRegister().getField(FIELD_REGISTER_COUNT_NAME);
        } catch (FieldNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Field<Integer> getFieldRegisterCount() {
        return fieldRegisterCount;
    }

    public static Register9999 create(Context context){
        return new Register9999(context);
    }
}
