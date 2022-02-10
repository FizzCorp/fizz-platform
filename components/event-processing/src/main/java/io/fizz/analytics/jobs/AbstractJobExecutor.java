package io.fizz.analytics.jobs;

import io.fizz.common.ConfigService;
import io.fizz.common.LoggingService;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

import java.util.Objects;

public abstract class AbstractJobExecutor {
    private static LoggingService.Log logger = LoggingService.getLogger(AbstractJobExecutor.class);

    protected SparkSession spark;
    protected String dataPath;
    protected String outputPath;
    protected String tempDataPath;

    protected static AbstractJobExecutor instance = null;

    public void init() {
        final ConfigService configService = ConfigService.instance();
        final boolean useResources = configService.getBoolean("hive2tsdb.hive.useResources");
        final String dataRoot = configService.getString("hive2tsdb.hive.dataRoot");
        final String tempDataRoot = configService.getString("hive2tsdb.hive.tempDataRoot");
        final String dataPath = useResources ? getResourcesFilePath("") + dataRoot : dataRoot;
        final String tempDataPath = useResources ? getResourcesFilePath("") + tempDataRoot : tempDataRoot;
        final String outPath = configService.hasKey("hive2tsdb.hive.outputRoot") ? configService.getString("hive2tsdb.hive.outputRoot") : null;
        final String sparkMaster = configService.getString("hive2tsdb.spark.master");
        final String s3AccessKeyId = configService.getString("aws.s3n.accessKeyId");
        final String s3SecretAccessKey = configService.getString("aws.s3n.secretAccessKey");

        logger.info("=== Initializing spark");

        SparkConf config = new SparkConf()
                .setAppName("Fizz Analytics")
                .setMaster(sparkMaster)
                .set("hive.exec.dynamic.partition.mode", "nonstrict")
                .set("hive.merge.sparkfiles", "true")
                .set("fs.s3n.awsAccessKeyId", s3AccessKeyId)
                .set("fs.s3n.awsSecretAccessKey", s3SecretAccessKey)
                .set("spark.debug.maxToStringFields", "99999")
                .setExecutorEnv("spark.sql.warehouse.dir", dataPath);

        onConfigCreated(config);

        spark = SparkSession
                .builder()
                .config(config)
                .enableHiveSupport()
                .getOrCreate();

        this.dataPath = dataPath;
        this.tempDataPath = tempDataPath;
        this.outputPath = Objects.isNull(outPath) ? dataPath : outPath;
    }

    protected void onConfigCreated(final SparkConf conf) {}

    public void execute() throws Exception {
        logger.info("Executing job...");
    }

    /*
    public static void main (String[] args) throws Exception {
        if (instance == null) {
            logger.error("Executor instance not specified...");
            return;
        }

        instance.init();
        instance.execute();

        logger.info("=== exiting application");
    }
    */

    private static String getResourcesFilePath (String file) {
        final String rootDir = System.getProperty("user.dir");
        return (rootDir + "/src/main/resources/" + file).replace("\\", "/");
    }
}
