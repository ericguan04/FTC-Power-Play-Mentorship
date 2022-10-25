package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

public class Power_Play_TeleOp extends OpMode {

    //SLIDER ENCODER CALCULATIONS
    static final double COUNTS_PER_MOTOR_REV = 751.8;        // TICKS PER REVOLUTION FOR GOBILDA 5203 223RPM MOTOR
    static final double DRIVE_GEAR_REDUCTION = 1.0;          // This is < 1.0 if geared UP
    static final double PULLY_HUB_DIAMETER_INCHES = 1.375;   // For figuring circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                                          (PULLY_HUB_DIAMETER_INCHES * 3.1415);
    static final double ARM_SPEED = 1;
    static final double FIRST_LEVEL_INCHES  = 4.2096;         // HEIGHT FOR FIRST LEVEL IN INCHES
    static final double SECOND_LEVEL_INCHES = 9.2341;        // HEIGHT FOR SECOND LEVEL IN INCHES
    static final double THIRD_LEVEL_INCHES  = 15.2090;        // HEIGHT FOR THIRD LEVEL IN INCHES

    //CALCULATED NUMBER OF TICKS USED TO MOVE THE SLIDE 'X' INCHES
    final int LIFT_LEVEL_ORIGINAL = 0;
    final int LIFT_LEVEL_ZERO = 50;
    final int LIFT_LEVEL_ONE = (int) (FIRST_LEVEL_INCHES * COUNTS_PER_INCH);
    final int LIFT_LEVEL_TWO = (int) (SECOND_LEVEL_INCHES * COUNTS_PER_INCH);
    final int LIFT_LEVEL_THREE = (int) (THIRD_LEVEL_INCHES * COUNTS_PER_INCH);

    //FINITE STATE MACHINE SETUP
    public enum LiftState {
    LIFT_START, LIFT_EXTEND_ZERO, LIFT_IDLE, LIFT_EXTEND_ONE,
        LIFT_EXTEND_TWO, LIFT_EXTEND_THREE, LIFT_DROP, LIFT_EXTEND_ORIGINAL
    }
    LiftState liftState = LiftState.LIFT_START;

    //DECLARE MOTORS FOR DRIVETRAIN
    public DcMotor frontLeft;
    public DcMotor backLeft;
    public DcMotor frontRight;
    public DcMotor backRight;

    //DECLARE MOTORS AND SERVOS FOR SLIDE
    public DcMotor armMotor;          // MOTOR FOR THE SLIDE
    public Servo claw;                // SERVO FOR THE CLAW

    final double CLAW_OPEN = 0.0;     // SERVO POSITION TO OPEN CLAW
    final double CLAW_CLOSE = 0.0;    // SERVO POSITION TO CLOSE CLAW

    public void init() {
        //INITIALIZES ALL MOTORS AND DEFAULTS SETTINGS
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        backLeft   = hardwareMap.dcMotor.get("backLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backRight  = hardwareMap.dcMotor.get("backRight");

        armMotor   = hardwareMap.dcMotor.get("armMotor");

        claw       = hardwareMap.servo.get("claw");

        //REVERSE MOTORS IF NECESSARY
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);
        armMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        //SETS THE ENCODERS ON THE SLIDE TO DEFAULT VALUES
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //SETS THE CLAW IN DEFAULT POSITION
        claw.setPosition(CLAW_OPEN);

        //MAKES DRIVETRAIN MORE PRECISE (FORCES MOTORS TO BRAKE WHEN STOPPED)
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

    }

    public void loop() {
        switch (liftState) {
            case LIFT_START:
                if (gamepad1.left_bumper) {
                    claw.setPosition(CLAW_CLOSE);
                    armMotor.setTargetPosition(LIFT_LEVEL_ZERO);
                    armMotor.setPower(ARM_SPEED);
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_EXTEND_ZERO;
                }
                break;
            case LIFT_EXTEND_ZERO:
                if (Math.abs(armMotor.getCurrentPosition() - LIFT_LEVEL_ZERO) < 10) {
                    armMotor.setPower(0);
                    liftState = LiftState.LIFT_IDLE;
                }
                break;
            case LIFT_IDLE:
                if (gamepad1.a) {
                    armMotor.setTargetPosition(LIFT_LEVEL_ONE);
                    armMotor.setPower(ARM_SPEED);
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_EXTEND_ONE;
                }

                if (gamepad1.b) {
                    armMotor.setTargetPosition(LIFT_LEVEL_TWO);
                    armMotor.setPower(ARM_SPEED);
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_EXTEND_TWO;
                }

                if (gamepad1.y) {
                    armMotor.setTargetPosition(LIFT_LEVEL_THREE);
                    armMotor.setPower(ARM_SPEED);
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_EXTEND_THREE;
                }
                break;
            case LIFT_EXTEND_ONE:
                if (Math.abs(armMotor.getCurrentPosition() - LIFT_LEVEL_ONE) < 10) {
                    armMotor.setPower(0);
                    liftState = LiftState.LIFT_DROP;
                }
                break;
            case LIFT_EXTEND_TWO:
                if (Math.abs(armMotor.getCurrentPosition() - LIFT_LEVEL_TWO) < 10) {
                    armMotor.setPower(0);
                    liftState = LiftState.LIFT_DROP;
                }
                break;
            case LIFT_EXTEND_THREE:
                if (Math.abs(armMotor.getCurrentPosition() - LIFT_LEVEL_THREE) < 10) {
                    armMotor.setPower(0);
                    liftState = LiftState.LIFT_DROP;
                }
                break;
            case LIFT_DROP:
                if (gamepad1.right_bumper) {
                    claw.setPosition(CLAW_OPEN);
                    armMotor.setTargetPosition(LIFT_LEVEL_ORIGINAL);
                    armMotor.setPower(ARM_SPEED);
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_EXTEND_ORIGINAL;
                }

                if (gamepad1.a) {
                    armMotor.setTargetPosition(LIFT_LEVEL_ONE);
                    armMotor.setPower(ARM_SPEED);
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_EXTEND_ONE;
                }

                if (gamepad1.b) {
                    armMotor.setTargetPosition(LIFT_LEVEL_TWO);
                    armMotor.setPower(ARM_SPEED);
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_EXTEND_TWO;
                }

                if (gamepad1.y) {
                    armMotor.setTargetPosition(LIFT_LEVEL_THREE);
                    armMotor.setPower(ARM_SPEED);
                    armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_EXTEND_THREE;
                }
                break;
            case LIFT_EXTEND_ORIGINAL:
                if (Math.abs(armMotor.getCurrentPosition() - LIFT_LEVEL_ORIGINAL) < 10) {
                    armMotor.setPower(0);
                    liftState = LiftState.LIFT_START;
                }
                break;
            default:
                liftState = LiftState.LIFT_START;
        }
        //SAFETY BUTTON TO RESET THE SYSTEM BACK TO THE START
        if (gamepad1.x && liftState != LiftState.LIFT_START) {
            claw.setPosition(CLAW_OPEN);
            armMotor.setTargetPosition(LIFT_LEVEL_ORIGINAL);
            armMotor.setPower(ARM_SPEED);
            armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            liftState = LiftState.LIFT_EXTEND_ORIGINAL;
        }

        //CODE FOR DRIVE TRAIN
        double y = -gamepad1.left_stick_y; // Remember, this is reversed!
        double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
        double rx = gamepad1.right_stick_x;

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio, but only when
        // at least one is out of the range [-1, 1]
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        //divide by value greater than 1 to make it slower
        frontLeft.setPower(-frontLeftPower);
        backLeft.setPower(-backLeftPower);
        frontRight.setPower(-frontRightPower);
        backRight.setPower(-backRightPower);
    }
}
