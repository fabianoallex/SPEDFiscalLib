import java.util.ArrayList;
import java.util.Date;

public class Register implements Unit {
    private final String name;
    Writer writer;
    private final ArrayList<Register> registers;
    private final Fields fields;

    Register(String name, Writer writer){
        this.name = name;
        this.writer = writer;
        registers = new ArrayList<>();
        this.fields = Fields.createFields(name);
    }

    public Fields getFields() {
        return fields;
    }

    public Writer getWriter() {
        return writer;
    }

    Register addRegister(String name){
        Register register = new Register(name, writer);
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

        for (Unit unit : registers) {
            c += unit.count();
        }

        return c;
    }

    @Override
    public void validate() {
        //todo: implementar rotinas de validação dos registros
    }

    @Override
    public void write() {
        writer.write(this.toString());

        for (Register register : registers) {
            register.write();
        }
    }

    @Override
    public String toString() {
        return "%s%s%s%s".formatted(
                FieldDefinitions.FIELD_SEPARATOR,
                this.getName(),
                FieldDefinitions.FIELD_SEPARATOR,
                fields.toString()
        );
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public String getName() {
        return name;
    }

    public Field<?> getFieldByName(String fieldName){
        return getFields().getFieldByName(fieldName);
    }

    public StringField getStringField(String fieldName){
        return getFields().getStringFieldByName(fieldName);
    }

    public IntegerField getIntegerField(String fieldName){
        return getFields().getIntegerFieldByName(fieldName);
    }

    public DoubleField getDoubleField(String fieldName){
        return getFields().getDoubleFieldByName(fieldName);
    }

    public DateField getDateField(String fieldName){
        return getFields().getDateFieldByName(fieldName);
    }

    public void setFieldValue(String name, String value) {
        this.getStringField(name).setValue(value);
    }

    public void setFieldValue(String name, Double value) {
        this.getDoubleField(name).setValue(value);
    }

    public void setFieldValue(String name, int value) {
        this.getIntegerField(name).setValue(value);
    }

    public void setFieldValue(String name, Date date) {
        this.getDateField(name).setValue(date);
    }
}
