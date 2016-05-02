package org.apache.curator.framework.schema;

import com.google.common.base.Preconditions;
import org.apache.zookeeper.CreateMode;
import java.util.regex.Pattern;

public class Schema
{
    private final Pattern path;
    private final String documentation;
    private final DataValidator dataValidator;
    private final Allowance ephemeral;
    private final Allowance sequential;
    private final boolean canBeWatched;
    private final boolean canHaveChildren;
    private final boolean canBeDeleted;

    public enum Allowance
    {
        CAN,
        MUST,
        CANNOT
    }

    public static SchemaBuilder builder(String path)
    {
        return new SchemaBuilder(Pattern.compile(path));
    }

    public Schema(Pattern path, String documentation, DataValidator dataValidator, Allowance ephemeral, Allowance sequential, boolean canBeWatched, boolean canHaveChildren, boolean canBeDeleted)
    {
        this.path = Preconditions.checkNotNull(path, "path cannot be null");
        this.documentation = Preconditions.checkNotNull(documentation, "documentation cannot be null");
        this.dataValidator = Preconditions.checkNotNull(dataValidator, "dataValidator cannot be null");
        this.ephemeral = Preconditions.checkNotNull(ephemeral, "ephemeral cannot be null");
        this.sequential = Preconditions.checkNotNull(sequential, "sequential cannot be null");
        this.canBeWatched = canBeWatched;
        this.canHaveChildren = canHaveChildren;
        this.canBeDeleted = canBeDeleted;
    }

    public void validateDeletion()
    {
        if ( !canBeDeleted )
        {
            throw new SchemaViolation(this, "Cannot be deleted");
        }
    }

    public void validateWatcher(boolean isWatching)
    {
        if ( isWatching && !canBeWatched )
        {
            throw new SchemaViolation(this, "Cannot be watched");
        }
    }

    public void validateCreate(CreateMode mode, byte[] data)
    {
        if ( mode.isEphemeral() && (ephemeral == Allowance.CANNOT) )
        {
            throw new SchemaViolation(this, "Cannot be ephemeral");
        }

        if ( !mode.isEphemeral() && (ephemeral == Allowance.MUST) )
        {
            throw new SchemaViolation(this, "Must be ephemeral");
        }

        if ( mode.isSequential() && (sequential == Allowance.CANNOT) )
        {
            throw new SchemaViolation(this, "Cannot be sequential");
        }

        if ( !mode.isSequential() && (sequential == Allowance.MUST) )
        {
            throw new SchemaViolation(this, "Must be sequential");
        }

        validateData(data);
    }

    public void validateData(byte[] data)
    {
        if ( !dataValidator.isValid(data) )
        {
            throw new SchemaViolation(this, "Data is not valid");
        }
    }

    public Pattern getPath()
    {
        return path;
    }

    public String getDocumentation()
    {
        return documentation;
    }

    public DataValidator getDataValidator()
    {
        return dataValidator;
    }

    public Allowance getEphemeral()
    {
        return ephemeral;
    }

    public Allowance getSequential()
    {
        return sequential;
    }

    public boolean canBeWatched()
    {
        return canBeWatched;
    }

    public boolean canHaveChildren()
    {
        return canHaveChildren;
    }

    public boolean canBeDeleted()
    {
        return canBeDeleted;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Schema schema = (Schema)o;

        return path.equals(schema.path);

    }

    @Override
    public int hashCode()
    {
        return path.hashCode();
    }

    @Override
    public String toString()
    {
        return "Schema{" +
            "path=" + path +
            ", documentation='" + documentation + '\'' +
            ", dataValidator=" + dataValidator +
            ", isEphemeral=" + ephemeral +
            ", isSequential=" + sequential +
            ", canBeWatched=" + canBeWatched +
            ", canHaveChildren=" + canHaveChildren +
            ", canBeDeleted=" + canBeDeleted +
            '}';
    }

    public String toDocumentation()
    {
        return path.pattern() + '\n'
            + documentation + '\n'
            + "Validator: " + dataValidator.getClass().getSimpleName() + '\n'
            + String.format("ephemeral: %s | sequential: %s | canBeWatched: %s | canHaveChildren: %s | canBeDeleted: %s", ephemeral, sequential, canBeWatched, canHaveChildren, canBeDeleted) + '\n'
            ;
    }
}