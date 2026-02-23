import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useProduct } from '../hooks/useProduct'
import { useAuth } from '../contexts/AuthContext'

export default function ProductFormPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { role } = useAuth()
  const isEdit = !!id
  const { product, loading, error: loadError, saving, createProduct, updateProduct } = useProduct(
    isEdit ? Number(id) : undefined
  )

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('')
  const [sku, setSku] = useState('')
  const [unitOfMeasure, setUnitOfMeasure] = useState('')
  const [basePrice, setBasePrice] = useState('')
  const [availableQuantity, setAvailableQuantity] = useState('')
  const [currency, setCurrency] = useState('USD')
  const [submitError, setSubmitError] = useState<string | null>(null)

  // Redirect buyers away
  useEffect(() => {
    if (role === 'BUYER') navigate('/catalog', { replace: true })
  }, [role, navigate])

  // Pre-populate form in edit mode
  useEffect(() => {
    if (product) {
      setName(product.name)
      setDescription(product.description || '')
      setCategory(product.category)
      setSku(product.sku)
      setUnitOfMeasure(product.unitOfMeasure)
      setBasePrice(String(product.basePrice))
      setAvailableQuantity(String(product.availableQuantity))
      setCurrency(product.currency)
    }
  }, [product])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSubmitError(null)

    const request = {
      name: name.trim(),
      description: description.trim() || undefined,
      category: category.trim(),
      sku: sku.trim(),
      unitOfMeasure: unitOfMeasure.trim(),
      basePrice: Number(basePrice),
      availableQuantity: Number(availableQuantity),
      currency,
    }

    try {
      if (isEdit) {
        await updateProduct(Number(id), request)
      } else {
        await createProduct(request)
      }
      navigate('/my-products')
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to save product')
    }
  }

  if (role === 'BUYER') return null

  if (isEdit && loading) {
    return <div className="flex items-center justify-center h-64 text-gray-400">Loading product...</div>
  }

  if (isEdit && loadError) {
    return <div className="bg-red-50 text-red-600 p-4 rounded-lg">{loadError}</div>
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">{isEdit ? 'Edit Product' : 'Add Product'}</h1>

      <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 max-w-2xl space-y-4">
        {submitError && (
          <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm">{submitError}</div>
        )}

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            maxLength={255}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none"
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Category *</label>
            <input
              type="text"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              required
              maxLength={100}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">SKU *</label>
            <input
              type="text"
              value={sku}
              onChange={(e) => setSku(e.target.value)}
              required
              maxLength={100}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Unit of Measure *</label>
            <input
              type="text"
              value={unitOfMeasure}
              onChange={(e) => setUnitOfMeasure(e.target.value)}
              required
              maxLength={50}
              placeholder="e.g. kg, unit, box"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Base Price *</label>
            <input
              type="number"
              value={basePrice}
              onChange={(e) => setBasePrice(e.target.value)}
              required
              min="0.01"
              step="0.01"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Available Quantity *</label>
            <input
              type="number"
              value={availableQuantity}
              onChange={(e) => setAvailableQuantity(e.target.value)}
              required
              min="0"
              step="1"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Currency</label>
          <select
            value={currency}
            onChange={(e) => setCurrency(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
          >
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="GBP">GBP</option>
          </select>
        </div>

        <div className="flex gap-3 pt-2">
          <button
            type="submit"
            disabled={saving}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg text-sm hover:bg-blue-700 transition-colors disabled:opacity-50"
          >
            {saving ? 'Saving...' : isEdit ? 'Update Product' : 'Create Product'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/my-products')}
            className="text-gray-500 px-4 py-2 text-sm hover:text-gray-700"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
