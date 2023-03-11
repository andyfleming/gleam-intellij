package run.gleam.lang.core.stubs

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.util.io.DataInputOutputUtil
import run.gleam.lang.GleamLanguage
import run.gleam.lang.core.parser.GleamParserDefinition
import run.gleam.lang.core.psi.GleamFile
import run.gleam.lang.core.psi.GleamFunction
import run.gleam.lang.core.psi.impl.GleamFunctionImpl
import run.gleam.lang.core.psi.impl.GleamFunctionReturnTypeImpl

class GleamFileStub(
    file: GleamFile?,
    private val flags: Int
) : PsiFileStubImpl<GleamFile>(file) {
    object Type : IStubFileElementType<GleamFileStub>(GleamLanguage) {
        // Bump this number if Stub structure changes
        private const val STUB_VERSION = 1
        override fun getStubVersion(): Int = GleamParserDefinition.PARSER_VERSION + STUB_VERSION

        override fun getExternalId(): String = "gleam.file"
    }
}

fun factory(name: String): GleamStubElementType<*, *> = when (name) {
    "FUNCTION" -> GleamFunctionStub.Type
    "FUNCTION_RETURN_TYPE" -> GleamPlaceholderStub.Type("FUNCTION_RETURN_TYPE", ::GleamFunctionReturnTypeImpl)
    else -> error("Unknown element $name")
}

class GleamFunctionStub(
    parent: StubElement<*>?, elementType: IStubElementType<*, *>,
    override val name: String?
) : GleamNamedStub, GleamElementStub<GleamFunction>(parent, elementType) {
    object Type: GleamStubElementType<GleamFunctionStub, GleamFunction>("FUNCTION") {
        override fun serialize(stub: GleamFunctionStub, dataStream: StubOutputStream) =
            with(dataStream) {
                writeName(stub.name)
            }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): GleamFunctionStub =
            GleamFunctionStub(
                parentStub,
                this,
                dataStream.readName()?.string,
            )

        override fun createStub(psi: GleamFunction, parentStub: StubElement<out PsiElement>?): GleamFunctionStub =
            GleamFunctionStub(
                parentStub,
                this,
                psi.identifier.text
            )

        override fun createPsi(stub: GleamFunctionStub): GleamFunction = GleamFunctionImpl(stub, this)

        override fun indexStub(stub: GleamFunctionStub, sink: IndexSink) = sink.indexFunction(stub)

    }
}

private fun StubInputStream.readNameAsString(): String? = readName()?.string
private fun StubInputStream.readUTFFastAsNullable(): String? = DataInputOutputUtil.readNullable(this, this::readUTFFast)
private fun StubOutputStream.writeUTFFastAsNullable(value: String?) =
    DataInputOutputUtil.writeNullable(this, value, this::writeUTFFast)

private fun StubOutputStream.writeLongAsNullable(value: Long?) =
    DataInputOutputUtil.writeNullable(this, value, this::writeLong)
private fun StubInputStream.readLongAsNullable(): Long? = DataInputOutputUtil.readNullable(this, this::readLong)

private fun StubOutputStream.writeDoubleAsNullable(value: Double?) =
    DataInputOutputUtil.writeNullable(this, value, this::writeDouble)
private fun StubInputStream.readDoubleAsNullable(): Double? = DataInputOutputUtil.readNullable(this, this::readDouble)
