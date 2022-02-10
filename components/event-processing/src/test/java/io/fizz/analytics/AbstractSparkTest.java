package io.fizz.analytics;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractSparkTest {
    protected static SparkSession spark;

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
    }

    private static String getResourcesFilePath (String file) {
        final String rootDir = System.getProperty("user.dir");
        return (rootDir + "/src/main/resources/" + file).replace("\\", "/");
    }
}
