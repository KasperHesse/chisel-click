import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def rgd_mutex_test(dut):
    """It should ensure mutual exclusion of two producers when using the RGD mutex"""
    #To test: Trigger R1, check that G1 is high.
    #Toggle D1, check that G1 is low

    #To test: Trigger R1, step a bit, check if g1 is high.
    #Trigger R2, step a bit, check if g2 is high


    # Reset
    dut.io_R1.value = 0
    dut.io_D1.value = 0
    dut.io_R2.value = 0
    dut.io_D2.value = 0
    dut.reset.value = 1
    await Timer(3, "ns")
    dut.reset.value = 0
    await Timer(3, "ns")

    #Toggle R1, should toggle G1
    dut.io_R1.value = 1
    await Timer(1, "ns")
    assert dut.io_G1.value == 1
    assert dut.io_G2.value == 0
    await Timer(1, "ns")

    #Toggle D1, should not toggle G1 (internal G1 has gone low, but not external)
    dut.io_D1.value = 1
    await Timer(1, "ns")
    assert dut.io_G1.value == 1
    assert dut.io_G2.value == 0

    #Toggle R2, should toggle G2
    dut.io_R2.value = 1
    await Timer(1, "ns")
    assert dut.io_G1.value == 1
    assert dut.io_G2.value == 1
    await Timer(1, "ns")

    #Toggle R1, should not toggle G1 because R2 has mutex right now
    dut.io_R1.value = 0
    await Timer(1, "ns")
    assert dut.io_G1.value == 1
    assert dut.io_G2.value == 1
    await Timer(1, "ns")

    #Toggle D2, should release mutex and grant it to R1
    dut.io_D2.value = 1
    await Timer(1, "ns")
    assert dut.io_G1.value == 0
    assert dut.io_G2.value == 1