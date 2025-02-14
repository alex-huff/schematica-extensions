package dev.phonis.schematica_extensions.util;

public class SchematicUtil
{

    public enum HitPositionWorld
    {
        REAL, SCHEMATIC, TIE, NONE;

        public boolean inSchematicWorld()
        {
            switch (this)
            {
                case SCHEMATIC:
                case TIE:
                    return true;
            }
            return false;
        }

        public boolean inRealWorld()
        {
            switch (this)
            {
                case REAL:
                case TIE:
                    return true;
            }
            return false;
        }
    }

}
