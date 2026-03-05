import AppRouter from './router'
import Navbar from './Navbar'

function App() {
  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />
      <main className="container mx-auto p-6">
        <AppRouter />
      </main>
    </div>
  )
}

export default App
