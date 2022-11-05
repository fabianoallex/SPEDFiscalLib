package SPEDFiscalLib;

import java.util.ArrayList;
import java.util.Map;

public class Register implements Unit {
    private final String name;
    private final ArrayList<Register> registers = new ArrayList<>();
    private final Fields fields;
    private final String referenceKey;
    private final Definitions definitions;

    Register(String name, Definitions definitions){
        this.name = name;
        this.definitions = definitions;
        this.fields = FieldsCreator.create(this.name, this.definitions);
        this.referenceKey = definitions.getRegisterDefinitions(this.name).key;
    }

    public <T> T getID() {
        if (this.referenceKey == null)
            return null;

        Field<?> field;
        try {
            field = this.getField(this.referenceKey);
        } catch (FieldNotFoundException e) {
            throw new RuntimeException(e);
        }

        return (T) field.getValue();
    }

    public Fields getFields() {
        return fields;
    }

    public Register addRegister(String name){
        Register register = new Register(name, this.definitions);
        this.registers.add(register);
        return register;
    }

    @Override
    public void count(Counter counter) {
        counter.increment(this.name);
        registers.forEach((unit -> unit.count(counter)));
    }

    @Override
    public int count() {
        int c = 1; //itself
        for (Unit unit : registers) c += unit.count();
        return c;
    }

    @Override
    public void validate(ValidationListener validationListener) {
        new RegisterValidator(this, validationListener).validate();

        for (Register register : registers)
            register.validate(validationListener);
    }

    @Override
    public void write(Writer writer) {
        writer.write(this.toString());
        for (Register register : registers) register.write(writer);
    }

    private String formatField(Field<?> field) {
        String fieldFormatName = this.getName() + "." + field.getName();
        FieldFormat fieldFormat = this.getDefinitions().getFieldFormatByFieldName(fieldFormatName);
        return FieldFormatter.formatField(field, fieldFormat);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilderFields = new StringBuilder();

        for (Map.Entry<String, Field<?>> e : fields.entrySet()) {
            Field<?> field = e.getValue();

            stringBuilderFields
                    .append(this.formatField(field))
                    .append(FieldDefinitions.FIELD_SEPARATOR);
        }

        return "%s%s%s%s".formatted(
                FieldDefinitions.FIELD_SEPARATOR,
                this.getName(),
                FieldDefinitions.FIELD_SEPARATOR,
                stringBuilderFields.toString()
        );
    }

    public String getName() {
        return name;
    }

    public <T extends Field<?>> T getField(String fieldName) throws FieldNotFoundException {
        Field<?> field = this.getFields().getField(fieldName);
        if (field == null) throw new FieldNotFoundException(Field.class.getSimpleName(), fieldName, this.getName());

        return (T) field;
    }

    public <T> void setFieldValue(String fieldName, T value) throws FieldNotFoundException {
        Field<T> field = this.getField(fieldName);
        field.setValue(value);
    }

    public <T> T getFieldValue(String fieldName) throws FieldNotFoundException {
        Field<T> field = this.getField(fieldName);
        return field.getValue();
    }

    protected Definitions getDefinitions() {
        return this.definitions;
    }
}

