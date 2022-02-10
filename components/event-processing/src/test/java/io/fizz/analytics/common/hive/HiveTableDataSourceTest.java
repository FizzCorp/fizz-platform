package io.fizz.analytics.common.hive;

import io.fizz.analytics.common.DayOfMonth;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.Month;
import io.fizz.analytics.common.Year;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

class HiveTableDataSourceTest {
    static class TestTableSchema extends AbstractHiveTableSchema {
        static final ColumnDesc columns[] = new ColumnDesc[] {
            new ColumnDesc("id", DataTypes.StringType, false, false),
            new ColumnDesc("dp", DataTypes.IntegerType, false, false),
            new ColumnDesc("year", DataTypes.StringType, false, true),
            new ColumnDesc("month", DataTypes.StringType, false, true),
            new ColumnDesc("day", DataTypes.StringType, false, true)
        };

        public static final String tableName = "testTable";

        public TestTableSchema() {
            super(tableName, RowFormat.JSON, columns);
        }
    }

    private static SparkSession spark;
    private static HiveTableDataSource table;
    private static TestTableSchema schema = new TestTableSchema();

    @BeforeAll
    static void setup() {
        SparkConf config = new SparkConf()
                .setAppName("Fizz Analytics")
                .setMaster("local[4]")
                .set("hive.exec.dynamic.partition.mode", "nonstrict")
                .set("hive.merge.sparkfiles", "true")
                .setExecutorEnv("spark.sql.warehouse.dir", getResourcesFilePath(""));

        spark = SparkSession
                .builder()
                .config(config)
                .enableHiveSupport()
                .getOrCreate();

        final URL resourceURL = HiveTableDataSourceTest.class.getClassLoader().getResource("hive/testTable");
        System.out.println(resourceURL);
        final String path = resourceURL.getPath();
        table = new HiveTableDataSource(spark, schema, path);
    }

    @Test
    @DisplayName("it should create table with valid data")
    void basicValidTableTest() {
        assert (table.getTableName().equals(TestTableSchema.tableName));
        assert (table.getSchema() == schema);
    }

    @Test
    @DisplayName("it should not create table with invalid spark context")
    void invalidSparkInstanceTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new HiveTableDataSource(null, null, null);
        });
    }

    @Test
    @DisplayName("it should not create table with invalid schema")
    void invalidSchemaTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new HiveTableDataSource(spark, null, null);
        });
    }

    @Test
    @DisplayName("it should not create table with invalid data path")
    void invalidDataPathTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new HiveTableDataSource(spark, new TestTableSchema(), null);
        });
    }

    @Test
    @DisplayName("it should scan complete table")
    void hiveTableScanTest() {
        final List<Row> rows = table.scan().collectAsList();

        assert (rows.size() == 4);

        Row row = rows.get(0);
        assert (row.getString(row.fieldIndex("id")).equals("16"));
        assert (row.getInt(row.fieldIndex("dp")) == 16);
        assert (row.getString(row.fieldIndex("year")).equals("2017"));
        assert (row.getString(row.fieldIndex("month")).equals("2017-12"));
        assert (row.getString(row.fieldIndex("day")).equals("2017-12-16"));

        row = rows.get(1);
        assert (row.getString(row.fieldIndex("id")).equals("1"));
        assert (row.getInt(row.fieldIndex("dp")) == 1);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-01"));

        row = rows.get(2);
        assert (row.getString(row.fieldIndex("id")).equals("2"));
        assert (row.getInt(row.fieldIndex("dp")) == 2);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-02"));

        row = rows.get(3);
        assert (row.getString(row.fieldIndex("id")).equals("31"));
        assert (row.getInt(row.fieldIndex("dp")) == 31);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-31"));
    }

    @Test
    @DisplayName("it should scan for day")
    void hiveTableDayScanTest() {
        final List<Row> rows = table.scan(new Year(2018), new Month(1), new DayOfMonth(1)).collectAsList();

        assert(rows.size() == 1);

        final Row row = rows.get(0);
        assert (row.getString(row.fieldIndex("id")).equals("1"));
        assert (row.getInt(row.fieldIndex("dp")) == 1);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-01"));
    }

    @Test
    @DisplayName("is should scan for last 2 days")
    void hiveTableScanLast2Days() {
        final List<Row> rows = table.scanForLastDaysFrom(2, new HiveTime(2018,1,3)).collectAsList();

        assert (rows.size() == 2);

        Row row = rows.get(0);
        assert (row.getString(row.fieldIndex("id")).equals("1"));
        assert (row.getInt(row.fieldIndex("dp")) == 1);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-01"));

        row = rows.get(1);
        assert (row.getString(row.fieldIndex("id")).equals("2"));
        assert (row.getInt(row.fieldIndex("dp")) == 2);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-02"));
    }

    @Test
    @DisplayName("is should scan for current month")
    void hiveTableScanCurrentMonth() {
        final List<Row> rows = table.scanForCurrentMonth(new HiveTime(2018,1,2)).collectAsList();

        assert (rows.size() == 2);

        Row row = rows.get(0);
        assert (row.getString(row.fieldIndex("id")).equals("1"));
        assert (row.getInt(row.fieldIndex("dp")) == 1);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-01"));

        row = rows.get(1);
        assert (row.getString(row.fieldIndex("id")).equals("2"));
        assert (row.getInt(row.fieldIndex("dp")) == 2);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-02"));
    }

    @Test
    @DisplayName("is should scan for current month")
    void hiveTableScanCurrentMonthFirstDay() {
        final List<Row> rows = table.scanForCurrentMonth(new HiveTime(2018,1,1)).collectAsList();

        assert (rows.size() == 1);

        Row row = rows.get(0);
        assert (row.getString(row.fieldIndex("id")).equals("1"));
        assert (row.getInt(row.fieldIndex("dp")) == 1);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-01"));
    }

    @Test
    @DisplayName("is should scan for current month")
    void hiveTableScanCurrentMonthLastDay() {
        final List<Row> rows = table.scanForCurrentMonth(new HiveTime(2018,1,31)).collectAsList();

        assert (rows.size() == 3);

        Row row = rows.get(0);
        assert (row.getString(row.fieldIndex("id")).equals("1"));
        assert (row.getInt(row.fieldIndex("dp")) == 1);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-01"));

        row = rows.get(2);
        assert (row.getString(row.fieldIndex("id")).equals("31"));
        assert (row.getInt(row.fieldIndex("dp")) == 31);
        assert (row.getString(row.fieldIndex("year")).equals("2018"));
        assert (row.getString(row.fieldIndex("month")).equals("2018-01"));
        assert (row.getString(row.fieldIndex("day")).equals("2018-01-31"));
    }

    @Test
    @DisplayName("is should scan for current month")
    void hiveTableScanMidMonthWindow() {
        List<Row> rows = table.scanForMonthWindow(new HiveTime(2018,1,31), 16).collectAsList();
        assert (rows.size() == 1);

        rows = table.scanForMonthWindow(new HiveTime(2018, 1, 16), 16).collectAsList();
        assert (rows.size() == 0);

        rows = table.scanForMonthWindow(new HiveTime(2018, 1, 15), 16).collectAsList();
        assert (rows.size() == 3);

        rows = table.scanForMonthWindow(new HiveTime(2017, 12, 16), 16).collectAsList();
        assert (rows.size() == 1);
    }

    private static String getResourcesFilePath (final String file) {
        final String rootDir = System.getProperty("user.dir");
        return (rootDir + "/src/main/resources/" + file).replace("\\", "/");
    }
}
