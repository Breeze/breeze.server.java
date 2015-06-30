package com.breeze.jpa;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Type.PersistenceType;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.breeze.metadata.Metadata;
import com.breeze.metadata.RawMetadata;

/**
 * Builds a data structure containing the metadata required by Breeze.
 * 
 * @see <a href="http://www.breezejs.com/documentation/breeze-metadata-format">Breeze Metadata Doc</a>
 * @author IdeaBlade
 *
 */
@SuppressWarnings("rawtypes")
public class JPAMetadata extends Metadata {

    private EntityManagerFactory _emFactory;
    private RawMetadata _rawMetadata;
    private List<HashMap<String, Object>> _typeList;
    private HashMap<String, Object> _resourceMap;
    private HashSet<String> _typeNames;
    private HashMap<String, String> _fkMap;

    public JPAMetadata(EntityManagerFactory emFactory) {
        _emFactory = emFactory;
    }


    /**
     * Build the raw Breeze metadata.  This will then get wrapped with a strongly typed wrapper. The internal
     * rawMetadata can be converted to JSON and sent to the Breeze client.
     */
    @Override
    public RawMetadata buildRawMetadata() {
        initMap();

        Set<ManagedType<?>> classMeta = _emFactory.getMetamodel().getManagedTypes();

        for (ManagedType<?> meta : classMeta) {
            addClass(meta);
        }

        return _rawMetadata;
    }

    
    void initMap() {
        _rawMetadata = new RawMetadata();
        _typeList = new ArrayList<HashMap<String, Object>>();
        _typeNames = new HashSet<String>();
        _resourceMap = new HashMap<String, Object>();
        _fkMap = new HashMap<String, String>();
        _rawMetadata.put("localQueryComparisonOptions", "caseInsensitiveSQL");
        _rawMetadata.put("structuralTypes", _typeList);
        _rawMetadata.put("resourceEntityTypeMap", _resourceMap);
        _rawMetadata.foreignKeyMap = _fkMap;
    }

    /**
     * Add the metadata for an entity.
     * 
     * @param meta
     */
    void addClass(ManagedType<?> meta) {
        Class type = meta.getJavaType();

        String classKey = getEntityTypeName(type);
        HashMap<String, Object> cmap = new LinkedHashMap<String, Object>();
        _typeList.add(cmap);

        cmap.put("shortName", type.getSimpleName());
        cmap.put("namespace", type.getPackage().getName());

        if (meta instanceof IdentifiableType)
        {
            IdentifiableType<?> idmeta = (IdentifiableType) meta;
            IdentifiableType superMeta = idmeta.getSupertype();
            if (superMeta != null) {
                Class superClass = superMeta.getJavaType();
                cmap.put("baseTypeName", getEntityTypeName(superClass));
            }

            String genType = "None";
            if (idmeta.hasSingleIdAttribute()) {
                javax.persistence.metamodel.Type<?> idType = idmeta.getIdType();
                
                // This throws when id is a primitive
                //SingularAttribute<?,?> idAttr = idmeta.getId(idType.getJavaType());
                SingularAttribute<?,?> idAttr = null;
                for (SingularAttribute<?,?> testAttr : meta.getDeclaredSingularAttributes()) {
                    if (testAttr.isId()) {
                        idAttr = testAttr;
                        break;
                    }
                }
                
                Member member = idAttr.getJavaMember();
                GeneratedValue genValueAnn = ((AnnotatedElement)member).getAnnotation(GeneratedValue.class);
                if (genValueAnn != null) {
                    // String generator = genValueAnn.generator();
                    GenerationType strategy = genValueAnn.strategy();
                    if (strategy == GenerationType.SEQUENCE || strategy == GenerationType.TABLE) 
                        genType = "KeyGenerator";
                    else if (strategy == GenerationType.IDENTITY || strategy == GenerationType.AUTO)
                        genType = "Identity";  // not sure what to do about AUTO

                    cmap.put("autoGeneratedKeyType", genType);
                }
            }
        }

        String resourceName = pluralize(type.getSimpleName()); // TODO find the real name
        cmap.put("defaultResourceName", resourceName);
        _resourceMap.put(resourceName, classKey);

        ArrayList<HashMap<String, Object>> dataArrayList = new ArrayList<HashMap<String, Object>>();
        cmap.put("dataProperties", dataArrayList);
        ArrayList<HashMap<String, Object>> navArrayList = new ArrayList<HashMap<String, Object>>();
        cmap.put("navigationProperties", navArrayList);

        addClassProperties(meta, dataArrayList, navArrayList);
    }

    /**
     * Add the properties for an entity.
     * 
     * @param meta
     * @param pClass
     * @param dataArrayList - will be populated with the data properties of the entity
     * @param navArrayList - will be populated with the navigation properties of the entity
     */
    void addClassProperties(ManagedType<?> meta, ArrayList<HashMap<String, Object>> dataArrayList,
            ArrayList<HashMap<String, Object>> navArrayList) {

        for (SingularAttribute<?,?> attr : meta.getDeclaredSingularAttributes()) {
        
            String propName = attr.getName();
            PersistentAttributeType attribType = attr.getPersistentAttributeType();
            if (attribType == PersistentAttributeType.EMBEDDED) {
                // complex type
                if (attr.isId()) {
                    // need to map the fields individually
                    EmbeddableType<?> bed = _emFactory.getMetamodel().embeddable(attr.getJavaType());
                    
                    for (Attribute<?,?> battr : bed.getAttributes()) {
                        PersistentAttributeType attrType = attr.getPersistentAttributeType();
                        if (attrType == PersistentAttributeType.MANY_TO_ONE) {
                            // association type
                            HashMap<String, Object> assProp = makeAssociationProperty(battr, dataArrayList, true);
                            navArrayList.add(assProp);
                        } else {
                            // data property
                            SingularAttribute sbattr = (SingularAttribute) battr; 
                            HashMap<String, Object> dmap = makeDataProperty(battr.getName(), sbattr, sbattr.isOptional(), true, false);
                            dataArrayList.add(0, dmap);
                        }
                    }
                    
                } else {
                    String complexTypeName = addComponent(attr);
                    HashMap<String, Object> compMap = new HashMap<String, Object>();
                    compMap.put("nameOnServer", propName);
                    compMap.put("complexTypeName", complexTypeName);
                    compMap.put("isNullable", attr.isOptional());
                    dataArrayList.add(compMap);
                }
            } else if (attribType == PersistentAttributeType.BASIC) {
                // data property
                HashMap<String, Object> dmap = makeDataProperty(propName, attr, attr.isOptional(), attr.isId(), attr.isVersion());
                if (attr.isId()) 
                    dataArrayList.add(0, dmap);
                else
                    dataArrayList.add(dmap);
            } else {
                if (!attr.isAssociation()){
                    throw new RuntimeException("Can't handle attribute " + attr);
                }
                // Skip associations until below
            }
        }

        // We do the collection properties after the data properties, so we can
        // do the foreign key lookups
        for (Attribute<?,?> attr : meta.getDeclaredAttributes()) {
            if (attr.isAssociation()) {
                HashMap<String, Object> assProp = makeAssociationProperty(attr, dataArrayList, true);
                navArrayList.add(assProp);
            } else {
                // should have been handled above
            }
        }
    }

    boolean contains(int[] array, int x) {
        for (int j = 0; j < array.length; j++) {
            if (array[j] == x)
                return true;
        }
        return false;
    }

    boolean contains(String[] array, String x) {
        for (int j = 0; j < array.length; j++) {
            if (array[j].equals(x))
                return true;
        }
        return false;
    }

    /**
     * Adds a complex type definition
     * 
     * @param compType - The complex type
     * @param propColumns - The columns which the complex type spans. These are used to get length
     *            and defaultValues.
     * @return The class name and namespace of the component.
     */
    String addComponent(SingularAttribute sattr) {
        Class<?> type = sattr.getJavaType();

        // "Location:#com.breeze.model"
        String classKey = getEntityTypeName(type);
        if (_typeNames.contains(classKey)) {
            // Only add a complex type definition once.
            return classKey;
        }

        HashMap<String, Object> cmap = new LinkedHashMap<String, Object>();
        _typeList.add(0, cmap);
        _typeNames.add(classKey);

        cmap.put("shortName", type.getSimpleName());
        cmap.put("namespace", type.getPackage().getName());
        cmap.put("isComplexType", true);

        ArrayList<HashMap<String, Object>> dataArrayList = new ArrayList<HashMap<String, Object>>();
        cmap.put("dataProperties", dataArrayList);
        
        EmbeddableType<?> bed = _emFactory.getMetamodel().embeddable(type);
        
        for (Attribute<?,?> attrib : bed.getAttributes()) {
            PersistentAttributeType attrType = attrib.getPersistentAttributeType();
            if (!(attrib instanceof SingularAttribute)) {
                throw new RuntimeException("Collections not supported in complex types");
            }
            SingularAttribute cattr = (SingularAttribute) attrib; 
            if (attrType == PersistentAttributeType.EMBEDDED) {
                // nested complex type
                String complexTypeName = addComponent(cattr);
                HashMap<String, Object> compMap = new HashMap<String, Object>();
                compMap.put("nameOnServer", attrib.getName());
                compMap.put("complexTypeName", complexTypeName);
                compMap.put("isNullable", cattr.isOptional());
                dataArrayList.add(compMap);
            } else {
                // data property
                HashMap<String, Object> dmap = makeDataProperty(cattr.getName(), cattr, cattr.isOptional(), false, false);
                dataArrayList.add(dmap);
            }
        }

        return classKey;
    }

    /**
     * Make data property metadata for the entity
     * 
     * @param propName - name of the property on the server
     * @param type - data type of the property, e.g. Int32
     * @param col - the Column for this property; used for length and default value
     * @param isNullable - whether the property is nullable in the database
     * @param isKey - true if this property is part of the key for the entity
     * @param isVersion - true if this property contains the version of the entity (for a
     *            concurrency strategy)
     * @return data property definition
     */
    private HashMap<String, Object> makeDataProperty(String propName, SingularAttribute sattr, 
            boolean isNullable, boolean isKey, boolean isVersion) {
        Class type = sattr.getJavaType();
        String newType = BreezeTypeMap.get(type.getSimpleName().toLowerCase());
        String typeName = newType != null ? newType : type.getSimpleName();
        Member member = sattr.getJavaMember();
      

        HashMap<String, Object> dmap = new LinkedHashMap<String, Object>();
        dmap.put("nameOnServer", propName);
        
        Enumerated numer = ((AnnotatedElement)member).getAnnotation(Enumerated.class);
        if (numer != null) {
            if (numer.value() == EnumType.STRING) {
                typeName = "String";
            } else {
                typeName = "Byte";
            }
            dmap.put("enumType", type.getSimpleName());
        }
        dmap.put("dataType", typeName);
        dmap.put("isNullable", isNullable);

        
        if (isKey) {
            dmap.put("isPartOfKey", true);
        }
        if (isVersion) {
            dmap.put("concurrencyMode", "Fixed");
        }

        ArrayList<HashMap<String, String>> validators = new ArrayList<HashMap<String, String>>();

        if (!isNullable) {
            validators.add(newMap("name", "required"));
        }
        
        Column col = ((AnnotatedElement)member).getAnnotation(Column.class);
        
        if (col != null) {
            if (col.length() != 0) {
                dmap.put("maxLength", col.length());
                validators.add(newMap("maxLength", Integer.toString(col.length()), "name", "maxLength"));
            }
            if (col.precision() != 0) {
                dmap.put("precision", col.precision());
            }
            if (col.scale() != 0) {
                dmap.put("scale", col.scale());
            }
        }

        String validationType = ValidationTypeMap.get(typeName);
        if (validationType != null) {
            validators.add(newMap("name", validationType));
        }

        if (!validators.isEmpty())
            dmap.put("validators", validators);

        return dmap;
    }

    /**
     * Make a HashMap populated with the given key and value.
     * 
     * @param key
     * @param value
     * @return
     */
    static HashMap<String, String> newMap(String key, String value) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        return map;
    }

    static HashMap<String, String> newMap(String key, String value, String key2, String value2) {
        HashMap<String, String> map = newMap(key, value);
        map.put(key2, value2);
        return map;
    }

    /**
     * Make association property metadata for the entity. Also populates the _fkMap which is used
     * for related-entity fixup when saving.
     * 
     * @param containingPersister
     * @param propType
     * @param propName
     * @param dataProperties
     * @param isKey
     * @return association property definition
     */
    private HashMap<String, Object> makeAssociationProperty(Attribute attr, ArrayList<HashMap<String, Object>> dataProperties, boolean isKey) {
        HashMap<String, Object> nmap = new LinkedHashMap<String, Object>();
        String propName = attr.getName();
        nmap.put("nameOnServer", propName);

        Class relatedEntityType = getEntityType(attr);
        nmap.put("entityTypeName", getEntityTypeName(relatedEntityType));
        nmap.put("isScalar", !attr.isCollection());

        // the associationName must be the same at both ends of the association.
        Class containingType = attr.getDeclaringType().getJavaType();
        String[] columnNames = getColumnNames(attr);
        nmap.put("associationName",
                getAssociationName(containingType.getSimpleName(), relatedEntityType.getSimpleName(), columnNames));

        Member member = attr.getJavaMember();

        String[] fkNames = null;
        if (attr.isCollection()) {
            javax.persistence.metamodel.Type elementType = ((PluralAttribute) attr).getElementType();

            if (elementType.getPersistenceType() != PersistenceType.ENTITY) {
                throw new RuntimeException("Collection association " + attr + " has elementType " + elementType);
            }
            fkNames = getPropertyNamesForColumns((javax.persistence.metamodel.EntityType<?>) elementType, columnNames);
            if (fkNames != null) {
                nmap.put("invForeignKeyNamesOnServer", fkNames);
            }
        } else {
            // Not a collection type - a many-to-one or one-to-one association
            String entityRelationship = containingType.getName() + '.' + propName;
            
            fkNames = getPropertyNamesForColumns((javax.persistence.metamodel.EntityType<?>) attr.getDeclaringType(), columnNames);
            
            if (fkNames != null) {
                if (attr.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE) {
                    nmap.put("foreignKeyNamesOnServer", fkNames);
                } else if (attr.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE) {
                    OneToMany otm = ((AnnotatedElement)member).getAnnotation(OneToMany.class);
                    if (otm != null && otm.mappedBy() != null) {
                        fkNames = new String[]{ otm.mappedBy().toLowerCase() };
                        nmap.put("invForeignKeyNamesOnServer", fkNames);
                    } else {
                        nmap.put("foreignKeyNamesOnServer", fkNames);
                    }
                }

                // For many-to-one and one-to-one associations, save the relationship in _fkMap 
                // for re-establishing relationships during save
                _fkMap.put(entityRelationship, catColumnNames(fkNames, ','));

                if (isKey) {
                    for (String fkName : fkNames) {
                        HashMap<String, Object> relatedDataProperty = findPropertyByName(dataProperties, fkName);
                        if (!relatedDataProperty.containsKey("isPartOfKey")) {
                            relatedDataProperty.put("isPartOfKey", true);
                        }
                    }
                }
            }

            else if (fkNames == null) {
                nmap.put("foreignKeyNamesOnServer", columnNames);
                nmap.put("ERROR", "Could not find matching fk for property " + entityRelationship);
                _fkMap.put(entityRelationship, catColumnNames(columnNames, ','));
                throw new IllegalArgumentException("Could not find matching fk for property " + entityRelationship);
            }
        }

        return nmap;
    }

    /**
     * Get the type name in the form "Order:#northwind.model"
     * 
     * @param clazz
     * @return
     */
    String getEntityTypeName(Class clazz) {
        // return clazz.getName();
        return clazz.getSimpleName() + ":#" + clazz.getPackage().getName();
    }

    /**
     * Get the column names for a given property as an array of unbracketed,
     * lowercase names. For a collection property, the column name is the inverse foreign key (i.e.
     * the column on the other table that points back to the persister's table)
     */
    String[] getColumnNames(Attribute attr) {
        List<String> names = getAttributeColumnNames(attr);
        return unBracket(names.toArray(new String[names.size()]));
    }

    /**
     * Get the column names for the given attribute.  Recurses into embedded complex types.
     * @param attr
     * @return
     */
    List<String> getAttributeColumnNames(Attribute attr) {
        List<String> names = new ArrayList<String>();

        if (attr.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
            @SuppressWarnings("unchecked")
            EmbeddableType<?> bed = _emFactory.getMetamodel().embeddable(attr.getJavaType());
            for (Attribute<?,?> battr : bed.getAttributes()) {
                names.addAll(getAttributeColumnNames(battr)); // recursive call
            }
            return names;
        }
        
        Column col = ((AnnotatedElement)attr.getJavaMember()).getAnnotation(Column.class);
        if (col != null && col.name() != null) {
            names.add(col.name());
            return names;
        } else {
            JoinColumn jcol = ((AnnotatedElement)attr.getJavaMember()).getAnnotation(JoinColumn.class);
            if (jcol != null && jcol.name() != null) {
                names.add(jcol.name());
                return names;
            } else {
                JoinColumns jcols = ((AnnotatedElement)attr.getJavaMember()).getAnnotation(JoinColumns.class);
                if (jcols != null && jcols.value() != null) {
                    for (JoinColumn jjcol : jcols.value()) {
                        names.add(jjcol.name());
                    }
                    return names;
                } else {
                    names.add(attr.getName());
                    return names;
                }
            }
        } 
    }

    /**
     * Gets the properties matching the given columns.  May be a component, but will not be an association.
     * @param entityType
     * @param mappedBy
     * @param columnNames
     * @return
     */
    String[] getPropertyNamesForColumns(javax.persistence.metamodel.EntityType<?> entityType, String[] columnNames) {
        for (SingularAttribute attr : entityType.getSingularAttributes()) {
            String[] columnArray = getColumnNames(attr);
            if (namesEqual(columnArray, columnNames)) return new String[] { attr.getName() };
        }
        
        if (columnNames.length > 1)
        {
            // go one-by-one through columnNames, trying to find a matching property.
            // TODO: maybe this should split columnNames into all possible combinations of ordered subsets, and try those
            ArrayList<String> propList = new ArrayList<String>();
            String[] prop = new String[1];
            for (int i = 0; i < columnNames.length; i++)
            {
                prop[0] = columnNames[i];
                String[] names = getPropertyNamesForColumns(entityType, prop); // recursive call
                if (names != null) propList.addAll(Arrays.asList(names));
            }
            if (propList.size() > 0) return propList.toArray(new String[propList.size()]);
        }
        return null;
    }
    

    /**
     * Unbrackets the column names and concatenates them into a comma-delimited string
     */
    static String catColumnNames(String[] columnNames, char delim) {
        StringBuilder sb = new StringBuilder();
        for (String s : columnNames) {
            if (sb.length() > 0)
                sb.append(delim);
            sb.append(unBracket(s));
        }
        return sb.toString(); //.toLowerCase();
    }

    /**
     * Return true if the two arrays contain the same names, false otherwise.
     * Names are compared after unBracket(), and are case-insensitive.
     * @param a
     * @param b
     * @return
     */
    static boolean namesEqual(String[] a, String[] b)
    {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++)
        {
            if (!unBracket(a[i]).equalsIgnoreCase(unBracket(b[i]))) return false;
        }
        return true;
    }

    /**
     * Get the column name without square brackets or quotes around it. E.g. "[OrderID]" -> OrderID
     * Because sometimes Hibernate gives us brackets, and sometimes it doesn't. Double-quotes happen
     * with SQL CE. Backticks happen with MySQL.
     */
    static String unBracket(String name) {
        name = (name.charAt(0) == '[') ? name.substring(1, name.length() - 1) : name;
        name = (name.charAt(0) == '"') ? name.substring(1, name.length() - 1) : name;
        name = (name.charAt(0) == '`') ? name.substring(1, name.length() - 1) : name;
        return name;
    }

    /**
     * @return a new array containing the unbracketed names
     */
    static String[] unBracket(String[] names) {
        String[] u = new String[names.length];
        for (int i = 0; i < names.length; i++)
        {
            u[i] = unBracket(names[i]);
        }
        return u;
    }

    /**
     * Find the property in the list that has the given name.
     * @param properties list of DataProperty or NavigationProperty maps
     * @param name matched against the nameOnServer value of entries in the list
     * @return the found property map, or null if not found.
     */
    static HashMap<String, Object> findPropertyByName(ArrayList<HashMap<String, Object>> properties, String name)
    {
        Object nameOnServer;
        for (HashMap<String, Object> prop : properties)
        {
            nameOnServer = prop.get("nameOnServer");
            if (nameOnServer != null) {
                if (((String) nameOnServer).equalsIgnoreCase(name)) return prop;
            }
        }
        return null;
    }

    /**
     * Get the Breeze name of the entity type. For collections, Breeze expects the name of the
     * element type.
     * 
     * @param propType
     * @return
     */
    Class getEntityType(Attribute attr) {
        if (attr instanceof Bindable) {
            return ((Bindable)attr).getBindableJavaType();
        } else {
            throw new RuntimeException("Not Bindable: " + attr);
        }
    }

    /**
     * Lame pluralizer. Assumes we just need to add a suffix.
     */
    static String pluralize(String s) {
        if (s == null || s.isEmpty())
            return s;
        int last = s.length() - 1;
        char c = s.charAt(last);
        switch (c) {
        case 'y':
            return s.substring(0, last) + "ies";
        default:
            return s + 's';
        }
    }

    /**
     * Creates an association name from two entity names. For consistency, puts the entity names in
     * alphabetical order.
     * 
     * @param name1
     * @param name2
     * @param columnNames - name of the column(s) on the child entity
     * @return
     */
    static String getAssociationName(String name1, String name2, String[] columnNames) {
        String cols = catColumnNames(columnNames, '_');
        if (name1.compareTo(name2) < 0) return ASSN + name1 + '_' + name2 + '_' + cols;
        else return ASSN + name2 + '_' + name1 + '_' + cols;
    }

    static final String ASSN = "AN_";

    // Map of Hibernate datatype to Breeze datatype.
    static HashMap<String, String> BreezeTypeMap;

    // Map of data type to Breeze validation type
    static HashMap<String, String> ValidationTypeMap;

    // Set of Breeze types which don't need a maxlength validation
    static HashSet<String> NoLength;

    static {
        BreezeTypeMap = new HashMap<String, String>();
        BreezeTypeMap.put("byte[]", "Binary");
        BreezeTypeMap.put("binary", "Binary");
        BreezeTypeMap.put("binaryblob", "Binary");
        BreezeTypeMap.put("blob", "Binary");
        BreezeTypeMap.put("timestamp", "DateTime");
        BreezeTypeMap.put("timeastimespan", "Time");
        BreezeTypeMap.put("short", "Int16");
        BreezeTypeMap.put("integer", "Int32");
        BreezeTypeMap.put("long", "Int64");
        BreezeTypeMap.put("boolean", "Boolean");
        BreezeTypeMap.put("byte", "Byte");
        BreezeTypeMap.put("datetime", "DateTime");
        BreezeTypeMap.put("date", "DateTime");
        BreezeTypeMap.put("datetimeoffset", "DateTimeOffset");
        BreezeTypeMap.put("big_decimal", "Decimal");
        BreezeTypeMap.put("double", "Double");
        BreezeTypeMap.put("float", "Single");
        BreezeTypeMap.put("uuid", "Guid");
        BreezeTypeMap.put("uuid-char", "Guid");
        BreezeTypeMap.put("uuid-binary", "Guid");
        BreezeTypeMap.put("string", "String");
        BreezeTypeMap.put("time", "Time");

        NoLength = new HashSet<String>();
        NoLength.add("Byte");
        NoLength.add("Binary");
        NoLength.add("Int16");
        NoLength.add("Int32");
        NoLength.add("Int64");
        NoLength.add("DateTime");
        NoLength.add("DateTimeOffset");
        NoLength.add("Time");
        NoLength.add("Boolean");
        NoLength.add("Guid");
        NoLength.add("Double");
        NoLength.add("Single");
        NoLength.add("Decimal");

        ValidationTypeMap = new HashMap<String, String>();
        ValidationTypeMap.put("Boolean", "bool");
        ValidationTypeMap.put("Byte", "byte");
        ValidationTypeMap.put("DateTime", "date");
        ValidationTypeMap.put("DateTimeOffset", "date");
        ValidationTypeMap.put("Decimal", "number");
        ValidationTypeMap.put("Double", "number");
        ValidationTypeMap.put("Single", "number");
        ValidationTypeMap.put("Guid", "guid");
        ValidationTypeMap.put("Int16", "int16");
        ValidationTypeMap.put("Int32", "int32");
        ValidationTypeMap.put("Int64", "int64");
        ValidationTypeMap.put("Float", "number");
        // ValidationTypeMap.put("String", "string");
        ValidationTypeMap.put("Time", "duration");

    }
    
}
