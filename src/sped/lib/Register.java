package sped.lib;

import java.util.ArrayList;
import java.util.stream.Collectors;

final public class Register implements Unit {
    private final String name;
    private final ArrayList<Register> registers = new ArrayList<>();
    private final Fields fields;
    private final String referenceKey;
    private final Factory factory;

    Register(String name, Factory factory) {
        this.name = name;
        this.factory = factory;
        this.fields =  this.factory.createFields(name);
        this.referenceKey = this.factory.getDefinitions().getRegisterDefinitions(this.name).key;
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
        Register register = this.factory.createRegister(name);
        this.registers.add(register);
        return register;
    }

    public NamedRegister addNamedRegister(Class<? extends NamedRegister> clazz) {
        NamedRegister namedRegister = this.factory.createNamedRegister(clazz);
        this.registers.add(namedRegister.getRegister());
        return namedRegister;
    }

    @Override
    public void count(Counter counter) {
        counter.increment(this.name);
        registers.forEach((unit -> unit.count(counter)));
    }

    @Override
    public int count() {
        return registers.stream()
                .map(Register::count)
                .mapToInt(value -> value)
                .sum() + 1;
    }

    @Override
    public void validate(ValidationListener validationListener) {
        new RegisterValidator(this, validationListener).validate();
        registers.forEach(register -> register.validate(validationListener));
    }

    @Override
    public void write(Writer writer) {
        writer.write(this.toString(), this);
        registers.forEach(register -> register.write(writer));
    }

    @Override
    public String toString() {
        final String collect = fields.values()
                .stream()
                .map(field -> FieldFormatter.sanitizeField(field, this))
                .collect(Collectors.joining(this.factory.getDefinitions().getRegisterSeparator()));

        return "%s%s%s%s%s".formatted(
                this.factory.getDefinitions().getRegisterBeginEndSeparator(),
                this.getName(),
                this.factory.getDefinitions().getRegisterSeparator(),
                collect,
                this.factory.getDefinitions().getRegisterBeginEndSeparator()
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

    public Factory getFactory() {
        return this.factory;
    }
}

